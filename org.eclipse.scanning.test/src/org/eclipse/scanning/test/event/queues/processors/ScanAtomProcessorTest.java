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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
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
import org.eclipse.scanning.test.event.queues.dummy.DummyHasQueue;
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
		scanConsumer.setRunner(new FastRunCreator<ScanBean>(40, true)); 
		scanConsumer.start();
	}

	@Override
	protected void localTearDown() throws Exception {
		//Stop, disconnect & nullify Event infrastructure
		scanConsumer.stop();
		
		/*
		 * We only need to worry about still running beans throwing errors if there was 
		 * anything ever in  the status topic
		 */
		if (!scanConsumer.getStatusSet().isEmpty()) {
			waitForChildBeanState(Status.TERMINATED, 1000l);
		}
		
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
		}
		return scAt;
	}
	
	@Override
	protected Queueable getFailBean() {
		//Failure will be caused by setting status FAILED & message in causeFail()
		return getTestBean();
	}
	
	@Override
	protected void causeFail() throws Exception {
		//Pause the scan process...
		ScanBean scan = getLastChildBean();
		scan.setStatus(Status.REQUEST_PAUSE);
		IPublisher<ScanBean> processCommander = infrastructureServ.makePublisher(scanConsumer.getStatusTopicName());
		waitForChildBeanState(Status.RUNNING, 5000l);
		Thread.sleep(30);
		processCommander.broadcast(scan);
		waitForChildBeanState(Status.PAUSED, 5000l);
		
		//...inject a FAILED
		processCommander.setStatusSetName(scanConsumer.getStatusSetName()); //Why does this need to be set???
		scan = getLastChildBean();
		scan.setStatus(Status.FAILED);
		scan.setMessage("The badger apocalypse destroyed the detector");
		processCommander.broadcast(scan);
		waitForChildBeanState(Status.FAILED, 10000l);
		
		//Tidy up our fail causer
		processCommander.disconnect();
		System.out.println("\n**********************\n*** PROCESS FAILED ***\n**********************\n");
		
		//Pause consumer, wait, set message on ScanBean & status FAILED
	}
	
	protected void waitToTerminate() throws Exception {
		waitForChildBeanState(Status.RUNNING, 5000l);
		Thread.sleep(100);
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
		checkConsumerBean(Status.COMPLETE);
		ScanBean scan = getLastChildBean();

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
		checkConsumerBean(Status.TERMINATED);
	}
	
	@Override
	protected void processorSpecificFailTests() throws Exception {
		/*
		 * After fail:
		 * - child bean should have failed status
		 * - message from child queue should be set on parent
		 */
		checkConsumerBean(Status.FAILED);
		
		List<Queueable> broadBeans = getBroadcastBeans();
		DummyHasQueue lastBean = (DummyHasQueue) broadBeans.get(broadBeans.size()-1);
		assertEquals("Fail message on parent bean not same as on child bean", "'"+getLastChildBean().getName()+"': The badger apocalypse destroyed the detector", lastBean.getMessage());
		assertEquals("Fail queue message on parent bean not as expected", "Failure caused by '"+getLastChildBean().getName()+"'", lastBean.getQueueMessage());
	}
	
	private ScanBean getLastChildBean() throws Exception {
		return getLastChildBean(true);
	}
	
	private ScanBean getLastChildBean(boolean uniqueBean) throws Exception {
		List<ScanBean> statusSet = scanConsumer.getStatusSet();
		
		long startTime = System.currentTimeMillis(), runTime;
		while (statusSet.size() == 0) {
			Thread.sleep(50);
			statusSet = scanConsumer.getStatusSet();
			
			runTime = System.currentTimeMillis();
			if (startTime - runTime >= 10000) throw new Exception("No bean statuses in 10seconds");
				
		}
		if (uniqueBean) assertEquals("More than one bean in the status queue. Was it cleared?", statusSet.size(), 1);
		return statusSet.get(statusSet.size()-1);
	}
	
	/**
	 * Interrogate the statusSet of the scan consumer to check it had the 
	 * given final status.
	 * @param lastStatus
	 * @throws EventException
	 */
	private void checkConsumerBean(Status lastStatus) throws Exception {
		ScanBean lastBean = waitForChildBeanState(lastStatus, 5000);
		
		assertEquals("Unexpected ScanBean final status.", lastStatus, lastBean.getStatus());
		double lastBPercComp = lastBean.getPercentComplete();
		if (lastStatus == Status.COMPLETE) {
			assertEquals("ScanBean percent complete should be 100%.", 100d, lastBPercComp, 0);
		} else if (lastStatus == Status.TERMINATED || lastStatus == Status.FAILED) {
			//Last bean should be TERMINATED & not 100%
			assertTrue("ScanBean percent Complete should not be 100%", lastBPercComp != 100d);
			assertTrue("The percent complete is not between 0% & 100% (is: "+lastBPercComp+")", ((lastBPercComp > 0d) && (lastBPercComp < 100d)));
		} else {
			fail("Unknown bean final status");
		}
	}
	
	private ScanBean waitForChildBeanState(Status awaitedStatus, long timeout) throws Exception {
		final CountDownLatch statusLatch = new CountDownLatch(1);
		ISubscriber<IBeanListener<ScanBean>> statusSubsc = infrastructureServ.makeSubscriber(null);
		statusSubsc.addListener(new IBeanListener<ScanBean>() {

			@Override
			public void beanChangePerformed(BeanEvent<ScanBean> evt) {
				ScanBean bean = evt.getBean();
				if (bean.getStatus() == awaitedStatus) {
					statusLatch.countDown();
				}
			}

		});
		//In case the event already happened
		if (getLastChildBean().getStatus() == awaitedStatus) statusLatch.countDown();
		
		boolean unlatched = statusLatch.await(timeout, TimeUnit.MILLISECONDS);
		if (!unlatched) fail("Didn't get bean status before timeout.");
		statusSubsc.disconnect();
		
		return getLastChildBean();
	}

}
