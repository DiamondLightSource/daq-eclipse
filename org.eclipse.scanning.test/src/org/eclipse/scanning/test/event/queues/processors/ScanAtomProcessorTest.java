package org.eclipse.scanning.test.event.queues.processors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.net.URI;
import java.util.List;

import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.event.dry.DryRunCreator;
import org.eclipse.scanning.event.queues.beans.ScanAtom;
import org.eclipse.scanning.event.queues.processors.ScanAtomProcessor;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.test.event.queues.beans.util.TestAtomMaker;
import org.eclipse.scanning.test.event.queues.mocks.DummyQueueable;
import org.eclipse.scanning.test.event.queues.mocks.MockPublisher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public class ScanAtomProcessorTest extends AbstractQueueProcessorTest<QueueAtom> {
	
	private ScanAtom scAt;
	
	private IEventService evServ;
	private IProcessCreator<ScanBean> fakeRunner;
	private URI uri;
	
	private IConsumer<ScanBean> scanConsumer;
	private IPublisher<ScanBean> scanPublisher;
	
	@Before
	public void setup() throws Exception {
		//Create the event service
		uri = new URI("vm://localhost?broker.persistent=false");
		ActivemqConnectorService.setJsonMarshaller(new MarshallerService(new PointsModelMarshaller())); // <-- PointsModelMarshaller needed to serialize ScanRequests
		evServ = new EventServiceImpl(new ActivemqConnectorService());
		fakeRunner = new DryRunCreator<ScanBean>(true);

		//Create the scan consumer & publisher (these are ~real)
		scanConsumer = evServ.createConsumer(uri, IEventService.SUBMISSION_QUEUE,
				IEventService.STATUS_SET, IEventService.STATUS_TOPIC,
				IEventService.HEARTBEAT_TOPIC, IEventService.CMD_TOPIC);
		scanConsumer.setRunner(fakeRunner);
		scanConsumer.start();
		scanPublisher = evServ.createPublisher(uri, IEventService.STATUS_TOPIC);
		
		createStatusPublisher();
		
		scAt = TestAtomMaker.makeTestScanAtomA();
		scAt.setHostName(InetAddress.getLocalHost().getHostName());
		scAt.setUserName("abc12345");
		scAt.setBeamline("I15-1");
		
		scAt.setScanConsumerURI(uri.toString());
		scAt.setScanSubmitQueueName(IEventService.SUBMISSION_QUEUE);
		scAt.setScanStatusQueueName(IEventService.STATUS_SET);
		scAt.setScanStatusTopicName(IEventService.STATUS_TOPIC);
		
		processorSetup();
	}
	
	private void  processorSetup() {
		try {
			proc = new ScanAtomProcessor().makeProcessWithEvServ(scAt, statPub, true, evServ);
		} catch (EventException e) {
			System.out.println("Failed to create ScanAtomProcessor "+e);
		}
	}

	@After
	public void tearDown() throws Exception {
		scanPublisher.disconnect();
		
		scanConsumer.clearQueue(IEventService.SUBMISSION_QUEUE);
		scanConsumer.clearQueue(IEventService.STATUS_SET);
		scanConsumer.stop();
		scanConsumer.disconnect();
	}
	
	@Test
	public void testExecution() throws Exception {
		scAt.setName("Test Execution");
		doExecute();
		Thread.sleep(15000);
		
		/*
		 * After execution:
		 * - first bean in statPub should be Status.RUNNING & 0%
		 * - second bean in statPub should have Status.RUNNING & 5%
		 * - last bean in statPub should be Status.COMPLETE & 100%
		 * - consumer should have a ScanBean configured as the ScanAtom
		 *   - should be Status.COMPLETE and 100%
		 */
		
		Status[] reportedStatuses = new Status[]{Status.RUNNING, Status.RUNNING,
				Status.RUNNING, Status.RUNNING, Status.RUNNING};
		Double[] reportedPercent = new Double[]{0d, 0d, 
				1.25d, 2.5d, 5d};
		
		checkBeanStatuses(reportedStatuses, reportedPercent);
		checkBeanFinalStatus(Status.COMPLETE, true);
		
		//Thread.sleep(1000);
		checkConsumerBeans(Status.COMPLETE);
		
		//Assert we have a properly structured ScanBean
		List<ScanBean> statusSet = scanConsumer.getStatusSet();
		ScanBean scan = statusSet.get(statusSet.size()-1);
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
		assertEquals("Scan path definitions differ", scAt.getPathModels(), req.getModels());
		assertEquals("Detector definitions differ", scAt.getDetectorModels(), req.getDetectors());
		assertEquals("Monitor definitions differ", scAt.getMonitors(), req.getMonitorNames());
	}
	
	// Attempted to fix intermittent failure on travis.
	//@Test
	public void testInterruptedExecution() throws Exception {
		scAt.setName("Test Interrupted Execution");
		doExecute();
		
		//Wait to allow bean onto queue and then get the bean 
		Thread.sleep(2000);
		ScanBean scan = scanConsumer.getStatusSet().get(0);
		scan.setStatus(Status.REQUEST_TERMINATE);
		scan.setMessage("Emergency stop");
		scanPublisher.broadcast(scan);
		
		//Wait to allow the house to come crashing down
		Thread.sleep(3000);
		checkConsumerBeans(Status.TERMINATED);
		checkBeanFinalStatus(Status.REQUEST_TERMINATE, true);//Has to be request since using Mock
		
		//Check the message was correctly set too
		//(if this is after the terminate, we're also checking that no further processing is happening)
		List<DummyQueueable> broadcastBeans = ((MockPublisher<QueueAtom>)statPub).getBroadcastBeans();
		DummyQueueable lastBean = broadcastBeans.get(broadcastBeans.size()-1);
		assertEquals("queueMessage differs to expected", "Terminate called from 'Test Interrupted Execution' with message: 'Emergency stop'"
				, lastBean.getQueueMessage());
	}
	
	@Test
	public void testTermination() throws Exception {
		scAt.setName("Test Termination");
		doExecute();
		Thread.sleep(4000);
		proc.terminate();
		Thread.sleep(5000);
		
		/*
		 * After execution:
		 * - first bean in statPub should be Status.RUNNING
		 * - last bean in statPub should be Status.TERMINATED & not be 100%
		 * - last bean percentage should be 52.5% complete
		 * - consumer should have a ScanBean with Status.TERMINATED & 50% complete
		 */
		checkBeanFinalStatus(Status.TERMINATED);
		checkConsumerBeans(Status.TERMINATED);
	}
	
	@Test
	public void testErrorInScan() throws Exception {
		scAt.setName("Failed scan");
		processorSetup();
		doExecute();
		
		Thread.sleep(3000);
		ScanBean scan = scanConsumer.getStatusSet().get(0);
		scan.setStatus(Status.FAILED);
		scan.setMessage("Error!");
		scanPublisher.broadcast(scan);
		System.out.println("**************************");
		System.out.println("*     FAIL BROADCAST     *");
		System.out.println("**************************");
		
		/*
		 * After execution:
		 * - first bean in statPub should be Status.RUNNING
		 * - last bean in statPub should be Status.FAILED and not be 100%
		 * - consumer should have a ScanBean with Status.FAILED & not 100%
		 */
		
		Thread.sleep(3000);
		checkBeanFinalStatus(Status.FAILED);
		
		//Try to end a bit more gracefully; TERMINATE before shutdown 
		scan.setStatus(Status.REQUEST_TERMINATE);
		scanPublisher.broadcast(scan);
		Thread.sleep(500);
		
		//Check the message was correctly set too
		List<DummyQueueable> broadcastBeans = ((MockPublisher<QueueAtom>)statPub).getBroadcastBeans();
		DummyQueueable lastBean = broadcastBeans.get(broadcastBeans.size()-1);
		assertEquals("queueMessage differs to expected", "Error in execution of 'Failed scan'. Message: 'Error!'"
				, lastBean.getQueueMessage());
	}
	
	private void checkConsumerBeans(Status lastStatus) throws EventException {
		List<ScanBean> statusSet = scanConsumer.getStatusSet();
		assertTrue("More than one bean in the status queue. Was it cleared?",statusSet.size() == 1);
		ScanBean lastBean = statusSet.get(statusSet.size()-1);
		
		if (lastStatus.equals(Status.COMPLETE)) {
			assertEquals("Unexpected ScanBean final status", lastStatus, lastBean.getStatus());
			assertEquals("ScanBean percentcomplete wrong", 100d, lastBean.getPercentComplete(), 0);
		} else if (lastStatus.equals(Status.TERMINATED)) {
			//Last bean should be TERMINATED & not 100%
			assertEquals("Unexpected last ScanBean final status", lastStatus, lastBean.getStatus());
			assertThat("ScanBean percentComplete is 100%", lastBean.getPercentComplete(), is(not(100d)));
		}
		else {
			fail("Unknown bean final status");
		}
	}

}
