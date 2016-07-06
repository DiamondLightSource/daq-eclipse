package org.eclipse.scanning.test.event.queues.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.dry.FastRunCreator;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.event.queues.QueueServicesHolder;
import org.eclipse.scanning.event.queues.beans.ScanAtom;
import org.eclipse.scanning.event.queues.processors.ScanAtomProcessor;
import org.eclipse.scanning.test.event.queues.util.EventInfrastructureFactoryService;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;

public class ScanAtomProcessorTest extends AbstractQueueProcessorTest {
	
	private ScanAtom scAt;
	private ScanAtomProcessor scProcr;
	
	private EventInfrastructureFactoryService infrastructureServ;
	private IConsumer<ScanBean> scanConsumer;
	private IPublisher<ScanBean> scanPublisher;

	@Override
	protected void localSetup() throws Exception {
		//Start the event infrastructure & put the EventService in the service holder
		infrastructureServ = new EventInfrastructureFactoryService();
		infrastructureServ.start(true);
		QueueServicesHolder.setEventService(infrastructureServ.getEventService());
		
		//Set up the consumer which will work on ScanBeans made by the ScanProcessor
		scanConsumer = infrastructureServ.makeConsumer(null, false);
		scanConsumer.setRunner(new FastRunCreator<ScanBean>(50, true)); 
		scanConsumer.start();
		
//		//Set up publisher which will be used in certain tests FIXME Only create for tests where needed. 
//		scanPublisher = infrastructureServ.makePublisher(null);
	}

	@Override
	protected void localTearDown() throws Exception {
		//Stop, disconnect & nullify Event infrastructure
		scanConsumer.stop();
		scanConsumer.clearQueue(IEventService.SUBMISSION_QUEUE);
		scanConsumer.clearQueue(IEventService.STATUS_SET);
		scanConsumer.clearQueue(IEventService.CMD_SET);
		scanConsumer.disconnect();
		if (!(scanPublisher == null)) scanPublisher.disconnect();
		infrastructureServ.stop();
	}

	@Override
	protected IQueueProcessor<? extends Queueable> getTestProcessor(boolean makeNew) {
		if (scProcr == null || makeNew) scProcr = new ScanAtomProcessor();
		return scProcr;
	}

	@Override
	protected Queueable getTestBean() {
		if (scAt == null) {
			List<IScanPathModel> scanAxes = new ArrayList<>();
			scanAxes.add(new StepModel("ocs", 290, 80, 10));
			scanAxes.add(new StepModel("xMotor", 150, 100, 5));

			Map<String, Object> detectors = new HashMap<>();
			detectors.put("pe", new MockDetectorModel(30d));

			List<String> monitors = new ArrayList<>();
			monitors.add("bpm3");
			monitors.add("i0");

			scAt = new ScanAtom("VT scan across sample", scanAxes, detectors); 

			try {
				scAt.setHostName(InetAddress.getLocalHost().getHostName());
			} catch (UnknownHostException ex) {
				System.out.println("WARNING: Failed to set hostname on bean. Continuing...");
			}
			scAt.setUserName("abc12345");
			scAt.setBeamline("I15-1");
			try {
				scAt.setScanBrokerURI(infrastructureServ.getURI().toString());
			} catch (Exception ex) {
				System.out.println("Failed to set broker URI"+ex.getMessage());
			}
			//		scAt.setScanConsumerURI(uri.toString());
			//		scAt.setScanSubmitQueueName(IEventService.SUBMISSION_QUEUE);
			//		scAt.setScanStatusQueueName(IEventService.STATUS_SET);
			//		scAt.setScanStatusTopicName(IEventService.STATUS_TOPIC);
		}
		return scAt;
	}
	
	@Override
	protected Queueable getFailBean() {
		//Failure will be caused by setting status FAILED & message in causeFail()
		return getTestBean();
	}
	
	@Override
	protected void causeFail() {
		//Pause consumer, wait, set message on ScanBean & status FAILED
	}
	
	@Override
	protected void processorSpecificExecTests() throws Exception {
		/*
		 * After execution:
		 * - expect a specific series of reports from initial beans (creating ScanBean)
		 * - additional (repeated) check of last bean state, but with penultimate bean too
		 * - config of parent bean should be set on ScanBean
		 */
		Status[] reportedStatuses = new Status[]{Status.RUNNING, Status.RUNNING,
				Status.RUNNING, Status.RUNNING, Status.RUNNING};
		Double[] reportedPercent = new Double[]{0d, 0.75d, 
				2.5d, 4.25d, 5d};

		checkFirstBroadcastBeanStatuses(scAt, reportedStatuses, reportedPercent);
		checkLastBroadcastBeanStatuses(scAt, Status.COMPLETE, true);
		
		//Get the last bean from the consumer and check its status
		ScanBean scan = getLastChildBean();
		checkConsumerBean(scan, Status.COMPLETE);

		//Check the properties of the ScanAtom have been correctly passed down
		assertFalse("No beamline set", scan.getBeamline() == null);
		assertEquals("Incorrect beamline", scAt.getBeamline(), scan.getBeamline());
		assertFalse("No hostname set", scan.getHostName() == null);
		assertEquals("Incorrect hostname", scAt.getHostName(), scan.getHostName());
		assertFalse("No name set", scan.getName() == null);
		assertEquals("Incorrect name", scAt.getName(), scan.getName());
		assertFalse("No username set", scan.getUserName() == null);
		assertEquals("Incorrect username", scAt.getUserName(), scan.getUserName());
		//Check the ScanRequest itself has been correctly interpreted
		ScanRequest<?> req = scan.getScanRequest(); 
		assertEquals("Scan path definitions differ", scAt.getPathModels(), req.getCompoundModel().getModels());
		assertEquals("Detector definitions differ", scAt.getDetectorModels(), req.getDetectors());
		assertEquals("Monitor definitions differ", scAt.getMonitors(), req.getMonitorNames());
	}

	@Override
	protected void processorSpecificTermTests() throws Exception {
		/*
		 * After termination:
		 * - child bean should have been terminated
		 */
		ScanBean scan = getLastChildBean();
		checkConsumerBean(scan, Status.TERMINATED);
		
	}
	
	@Override
	protected void processorSpecificFailTests() throws Exception {
		/*
		 * After fail:
		 * - child bean should have failed status
		 * - message from child queue should be set on parent
		 */
		ScanBean scan = getLastChildBean();
		checkConsumerBean(scan, Status.FAILED);
		
		assertEquals("Fail message on parent bean not same as on child bean", "expmsg", "gotmsg");
	}
	
	private ScanBean getLastChildBean() throws EventException {
		List<ScanBean> statusSet = scanConsumer.getStatusSet();
		assertEquals("More than one bean in the status queue. Was it cleared?", statusSet.size(), 1);
		return statusSet.get(statusSet.size()-1);
	}
	
	/**
	 * Interrogate the statusSet of the scan consumer to check it had the 
	 * given final status.
	 * @param lastStatus
	 * @throws EventException
	 */
	private void checkConsumerBean(ScanBean lastBean, Status lastStatus) throws EventException {
		assertEquals("Unexpected ScanBean final status", lastStatus, lastBean.getStatus());
		double lastBPercComp = lastBean.getPercentComplete();
		if (lastStatus == Status.COMPLETE) {
			assertEquals("ScanBean percent complete should be 100%", 100d, lastBPercComp, 0);
		} else if (lastStatus == Status.TERMINATED || lastStatus == Status.FAILED) {
			//Last bean should be TERMINATED & not 100%
			assertTrue("ScanBean percent Complete should not be 100%", lastBPercComp != 100d);
			assertTrue("The percent complete is not between 0% & 100%", ((lastBPercComp > 0d) && (lastBPercComp < 100d)));
		} else {
			fail("Unknown bean final status");
		}
}
	
	
//	private ScanAtom scAt;
//	
//	private IEventService evServ;
//	private IProcessCreator<ScanBean> fakeRunner;
//
//	private IConsumer<ScanBean> scanConsumer;
//	private IPublisher<ScanBean> scanPublisher;
//	
//	@Before
//	public void setup() throws Exception {
//
//x		ActivemqConnectorService.setJsonMarshaller(new MarshallerService(new PointsModelMarshaller())); // <-- PointsModelMarshaller needed to serialize ScanRequests
//x		evServ = new EventServiceImpl(new ActivemqConnectorService());
//x		QueueServicesHolder.setEventService(evServ);
//x		fakeRunner = new DryRunCreator<ScanBean>(true);
//
//x		//Create the scan consumer & publisher (these are ~real)
//x		scanConsumer = evServ.createConsumer(uri, IEventService.SUBMISSION_QUEUE,
//x				IEventService.STATUS_SET, IEventService.STATUS_TOPIC,
//x				IEventService.HEARTBEAT_TOPIC, IEventService.CMD_TOPIC);
//x		scanConsumer.setRunner(fakeRunner);
//x		scanConsumer.start();
//x		scanPublisher = evServ.createPublisher(uri, IEventService.STATUS_TOPIC);
//		
//		createStatusPublisher();
//		
//		scAt = TestAtomMaker.makeTestScanAtomA();
//		scAt.setHostName(InetAddress.getLocalHost().getHostName());
//		scAt.setUserName("abc12345");
//		scAt.setBeamline("I15-1");
//		
//		scAt.setScanConsumerURI(uri.toString());
//		scAt.setScanSubmitQueueName(IEventService.SUBMISSION_QUEUE);
//		scAt.setScanStatusQueueName(IEventService.STATUS_SET);
//		scAt.setScanStatusTopicName(IEventService.STATUS_TOPIC);
//		
//		processorSetup();
//	}
//	
//	private void processorSetup() {
//		try {
//			proc = new ScanAtomProcessor().makeProcess(scAt, statPub, true);
//		} catch (EventException e) {
//			System.out.println("Failed to create ScanAtomProcessor "+e);
//		}
//	}
//
//	@After
//	public void tearDown() throws Exception {
//		scanPublisher.disconnect();
//		
//		scanConsumer.clearQueue(IEventService.SUBMISSION_QUEUE);
//		scanConsumer.clearQueue(IEventService.STATUS_SET);
//		scanConsumer.stop();
//		scanConsumer.disconnect();
//	}
//	
//	@Ignore
//	@Test
//	public void testExecution() throws Exception {
//		scAt.setName("Test Execution");
//		doExecute();
//		executionLatch.await(30, TimeUnit.SECONDS);
//		
//		/*
//		 * After execution:
//		 * - first bean in statPub should be Status.RUNNING & 0%
//		 * - second bean in statPub should have Status.RUNNING & 5%
//		 * - last bean in statPub should be Status.COMPLETE & 100%
//		 * - consumer should have a ScanBean configured as the ScanAtom
//		 *   - should be Status.COMPLETE and 100%
//		 */
//		
//		Status[] reportedStatuses = new Status[]{Status.RUNNING, Status.RUNNING,
//				Status.RUNNING, Status.RUNNING, Status.RUNNING};
//		Double[] reportedPercent = new Double[]{0d, 0d, 
//				1.25d, 2.5d, 5d};
//		
//		checkBeanStatuses(reportedStatuses, reportedPercent);
//		checkBeanFinalStatus(Status.COMPLETE, true);
//		
//		checkConsumerBeans(Status.COMPLETE);
//		
//		//Assert we have a properly structured ScanBean
//		List<ScanBean> statusSet = scanConsumer.getStatusSet();
//		ScanBean scan = statusSet.get(statusSet.size()-1);
//		//Check the properties of the ScanAtom have been correctly passed down
//		assertFalse("No beamline set", scan.getBeamline() == null);
//		assertEquals("Incorrect beamline", scAt.getBeamline(), scan.getBeamline());
//		assertFalse("No hostname set", scan.getHostName() == null);
//		assertEquals("Incorrect hostname", scAt.getHostName(), scan.getHostName());
//		assertFalse("No name set", scan.getName() == null);
//		assertEquals("Incorrect name", scAt.getName(), scan.getName());
//		assertFalse("No username set", scan.getUserName() == null);
//		assertEquals("Incorrect username", scAt.getUserName(), scan.getUserName());
//		//Check the ScanRequest itself has been correctly interpreted
//		ScanRequest<?> req = scan.getScanRequest(); 
//		assertEquals("Scan path definitions differ", scAt.getPathModels(), req.getModels());
//		assertEquals("Detector definitions differ", scAt.getDetectorModels(), req.getDetectors());
//		assertEquals("Monitor definitions differ", scAt.getMonitors(), req.getMonitorNames());
//	}
//	
//	// Attempted to fix intermittent failure on travis.
//	@Ignore
//	@Test
//	public void testTerminateFromScan() throws Exception {
//		scAt.setName("Test Interrupted Execution");
//		doExecute();
//		
//		//Wait to allow bean onto queue and then get the bean 
//		Thread.sleep(2000);
//		ScanBean scan = scanConsumer.getStatusSet().get(0);
//		scan.setStatus(Status.REQUEST_TERMINATE);
//		scan.setMessage("Emergency stop");
//		scanPublisher.broadcast(scan);
//		
//		//Wait to allow the house to come crashing down
//		pauseForStatus(Status.TERMINATED);
//		
//		checkConsumerBeans(Status.TERMINATED);
//		checkBeanFinalStatus(Status.REQUEST_TERMINATE, true);//Has to be request since using Mock
//		
//		//Check the message was correctly set too
//		//(if this is after the terminate, we're also checking that no further processing is happening)
//		List<DummyHasQueue> broadcastBeans = ((MockPublisher<QueueAtom>)statPub).getBroadcastBeans();
//		DummyHasQueue lastBean = broadcastBeans.get(broadcastBeans.size()-1);
//		assertEquals("queueMessage differs to expected", "Terminate called from 'Test Interrupted Execution' with message: 'Emergency stop'"
//				, lastBean.getQueueMessage());
//	}
//	
//	@Ignore
//	@Test
//	public void testTermination() throws Exception {
//		scAt.setName("Test Termination");
//		doExecute();
//		Thread.sleep(2000);
//		proc.terminate();
//
//		//Wait for terminate to take effect
//		pauseForStatus(Status.TERMINATED);
//		
//		/*
//		 * After execution:
//		 * - first bean in statPub should be Status.RUNNING
//		 * - last bean in statPub should be Status.TERMINATED & not be 100%
//		 * - last bean percentage should be 52.5% complete
//		 * - consumer should have a ScanBean with Status.TERMINATED & 50% complete
//		 */
//		checkBeanFinalStatus(Status.TERMINATED);
//		checkConsumerBeans(Status.TERMINATED);
//	}
//	
//	@Ignore
//	@Test
//	public void testFailureInScan() throws Exception {
//		scAt.setName("Failed scan");
//		doExecute();
//		
//		Thread.sleep(2000);
//		ScanBean scan = scanConsumer.getStatusSet().get(0);
//		scan.setStatus(Status.FAILED);
//		scan.setMessage("Error!");
//		scanPublisher.broadcast(scan);
//		System.out.println("**************************");
//		System.out.println("*     FAIL BROADCAST     *");
//		System.out.println("**************************");
//		
//		pauseForMockFinalStatus(10000);
//		/*
//		 * After execution:
//		 * - first bean in statPub should be Status.RUNNING
//		 * - last bean in statPub should be Status.FAILED and not be 100%
//		 * - consumer should have a ScanBean with Status.FAILED & not 100%
//		 */
//		
//		checkBeanFinalStatus(Status.FAILED);
//		
//		//Try to end a bit more gracefully; TERMINATE before shutdown 
//		scan.setStatus(Status.REQUEST_TERMINATE);
//		scanPublisher.broadcast(scan);
//		pauseForStatus(Status.TERMINATED);
//		
//		//Check the message was correctly set too
//		List<DummyHasQueue> broadcastBeans = ((MockPublisher<QueueAtom>)statPub).getBroadcastBeans();
//		DummyHasQueue lastBean = broadcastBeans.get(broadcastBeans.size()-1);
//		assertEquals("queueMessage differs to expected", "Error in execution of 'Failed scan'. Message: 'Error!'"
//				, lastBean.getQueueMessage());
//	}
//	
//	private void checkConsumerBeans(Status lastStatus) throws EventException {
//		List<ScanBean> statusSet = scanConsumer.getStatusSet();
//		assertTrue("More than one bean in the status queue. Was it cleared?",statusSet.size() == 1);
//		ScanBean lastBean = statusSet.get(statusSet.size()-1);
//		
//		if (lastStatus.equals(Status.COMPLETE)) {
//			assertEquals("Unexpected ScanBean final status", lastStatus, lastBean.getStatus());
//			assertEquals("ScanBean percentcomplete wrong", 100d, lastBean.getPercentComplete(), 0);
//		} else if (lastStatus.equals(Status.TERMINATED)) {
//			//Last bean should be TERMINATED & not 100%
//			assertEquals("Unexpected last ScanBean final status", lastStatus, lastBean.getStatus());
//			assertThat("ScanBean percentComplete is 100%", lastBean.getPercentComplete(), is(not(100d)));
//		}
//		else {
//			fail("Unknown bean final status");
//		}
//	}
//	
//	protected void pauseForStatus(Status awaitedStatus) throws Exception {
//		final CountDownLatch statusLatch = new CountDownLatch(1);
//		ISubscriber<IBeanListener<ScanBean>> statusSubsc = evServ.createSubscriber(uri, IEventService.STATUS_TOPIC);
//		statusSubsc.addListener(new IBeanListener<ScanBean>() {
//
//			@Override
//			public void beanChangePerformed(BeanEvent<ScanBean> evt) {
//				ScanBean bean = evt.getBean();
//				if (bean.getStatus() == awaitedStatus) {
//					statusLatch.countDown();
//				}
//			}
//			
//		});
//		statusLatch.await(10, TimeUnit.SECONDS);
//		return;
//	}

}
