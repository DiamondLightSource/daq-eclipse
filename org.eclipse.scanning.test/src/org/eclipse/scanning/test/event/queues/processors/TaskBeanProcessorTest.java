package org.eclipse.scanning.test.event.queues.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.alive.ConsumerCommandBean;
import org.eclipse.scanning.api.event.alive.PauseBean;
import org.eclipse.scanning.api.event.queues.IQueue;
import org.eclipse.scanning.api.event.queues.IQueueControllerService;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.QueueControllerService;
import org.eclipse.scanning.event.queues.QueueService;
import org.eclipse.scanning.event.queues.ServicesHolder;
import org.eclipse.scanning.event.queues.beans.SubTaskAtom;
import org.eclipse.scanning.event.queues.beans.TaskBean;
import org.eclipse.scanning.event.queues.processors.TaskBeanProcessor;
import org.eclipse.scanning.test.event.queues.mocks.MockConsumer;
import org.eclipse.scanning.test.event.queues.mocks.MockEventService;
import org.eclipse.scanning.test.event.queues.mocks.MockPublisher;
import org.eclipse.scanning.test.event.queues.mocks.MockSubmitter;
import org.eclipse.scanning.test.event.queues.util.TestAtomQueueBeanMaker;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TaskBeanProcessorTest {
	
	private TaskBean tBe;
	private TaskBeanProcessor tBeProcr;
	private ProcessorTestInfrastructure pti;
	
	private static QueueService qServ;
	private static MockConsumer<Queueable> mockCons;
	private static MockPublisher<QueueAtom> mockPub;
	private static MockPublisher<ConsumerCommandBean> mockCmdPub;
	private static MockSubmitter<QueueAtom> mockSub;
	private static MockEventService mockEvServ;
	private static IQueueControllerService controller;
	
	@BeforeClass
	public static void setUpClass() throws EventException {
		//Configure the processor Mock queue infrastructure
		mockCons = new MockConsumer<>();
		mockPub = new MockPublisher<>(null, null);
		mockCmdPub = new MockPublisher<>(null, null);
		mockSub = new MockSubmitter<>();
		mockSub.setSendToConsumer(true);
		mockEvServ = new MockEventService();
		mockEvServ.setMockConsumer(mockCons);
		mockEvServ.setMockPublisher(mockPub);
		mockEvServ.setMockCmdPublisher(mockCmdPub);
		mockEvServ.setMockSubmitter(mockSub);
		ServicesHolder.setEventService(mockEvServ);
		
		//This is a real queue service, so we have to do some set up
		try {
			URI uri = new URI("file:///foo/bar");
			qServ = new QueueService("fake-qserv", uri);
		} catch (URISyntaxException usEx) {
			//Shouldn't happen...
			usEx.printStackTrace();
		}
		qServ.init();
		qServ.start();
		
		ServicesHolder.setQueueService(qServ);
		
		//Once this lot is up, create a queue controller.
		controller = new QueueControllerService();
		controller.init();
		ServicesHolder.setQueueControllerService(controller);
	}
	
	@AfterClass
	public static void tearDownClass() {
		ServicesHolder.unsetEventService(mockEvServ);
		mockEvServ = null;
		mockPub = null;
		
		ServicesHolder.unsetQueueService(qServ);
		qServ = null;
		mockSub = null;
	}
	
	@Before
	public void setUp() {
		pti = new ProcessorTestInfrastructure();
		
		//Create processor & test atom
		tBe = new TaskBean("Test queue sub task bean");
		tBe.setBeamline("I15-1(test)");
		tBe.setHostName("afakeserver.diamond.ac.uk");
		tBe.setUserName(System.getProperty("user.name"));
		SubTaskAtom atomA = TestAtomQueueBeanMaker.makeDummySubTaskBeanA();
		SubTaskAtom atomB = TestAtomQueueBeanMaker.makeDummySubTaskBeanB();
		SubTaskAtom atomC = TestAtomQueueBeanMaker.makeDummySubTaskBeanC();
		tBe.addAtom(atomA);
		tBe.addAtom(atomB);
		tBe.addAtom(atomC);
		
		//Reset queue architecture
		mockSub.resetSubmitter();
		mockPub.resetPublisher();
	}
	
	@After
	public void tearDown() {
		pti = null;
	}
	
	@Test
	public void testExecution() throws Exception {
		tBeProcr = new TaskBeanProcessor();
		
		pti.executeProcessor(tBeProcr, tBe);
		
		assertTrue("Execute flag not set true after execution", tBeProcr.isExecuted());
		
		tBeProcr.getQueueBroadcaster().broadcast(Status.RUNNING, 99.5d, "Running finished.");
		tBeProcr.getProcessorLatch().countDown();
		pti.exceptionCheck();
		
		//These are the statuses & percent completes reported by the processor as it sets up the run
		Status[] reportedStatuses = new Status[]{Status.RUNNING, Status.RUNNING,
				Status.RUNNING, Status.RUNNING};
		Double[] reportedPercent = new Double[]{0d, 1d, 
				4d, 5d};
		
		pti.checkFirstBroadcastBeanStatuses(tBe, reportedStatuses, reportedPercent);
		pti.checkLastBroadcastBeanStatuses(tBe, Status.COMPLETE, true);
		
		checkSubmittedBeans(mockSub);
	}
	
	@Test
	public void testTermination() throws Exception {
		tBeProcr = new TaskBeanProcessor();
		
		pti.executeProcessor(tBeProcr, tBe);
		//Set some arbitrary percent complete
		tBeProcr.getQueueBroadcaster().broadcast(Status.REQUEST_TERMINATE, 20d);
		
		/*
		 * terminate is usually called as follows:
		 * AbstractPausableProcess.terminate() -> QueueProcess.doTerminate -> tBeProcr.terminate()
		 */
		pti.getQProc().terminate();
		pti.exceptionCheck();
		assertTrue("Terminated flag not set true after termination", tBeProcr.isTerminated());
		pti.checkLastBroadcastBeanStatuses(tBe, Status.TERMINATED, true);
		//TODO Should this be the message or the queue-message?
		assertEquals("Wrong message set after termination.", "Job-queue aborted before completion (requested)", pti.getLastBroadcastBean().getMessage());
		
	}
	
	@Test
	public void testChildFailure() throws Exception {
		tBeProcr = new TaskBeanProcessor();
		
		pti.executeProcessor(tBeProcr, tBe);
		//Set some arbitrary percent complete
		tBeProcr.getQueueBroadcaster().broadcast(Status.RUNNING, 20d);
		tBeProcr.getProcessorLatch().countDown();
		//Need to give the post-match analysis time to run
		Thread.sleep(10);
		
		/*
		 * FAILED is always going to happen underneath.
		 * QueueListener sets the message and queueMessage
		 * We need to set this bean's status to FAILED and pause the consumer 
		 * to stop running any more beans until the user is happy.
		 */
		pti.checkLastBroadcastBeanStatuses(tBe, Status.FAILED, false);
		
		//Check we sent a pause instruction to the job-queue consumer
		List<ConsumerCommandBean> cmdBeans = mockCmdPub.getCmdBeans();
		long timeout = 1000;
		while (cmdBeans.size() < 1) {
			//Sit here waiting until a cmd bean lands...
			Thread.sleep(50);
			timeout = timeout-50;
			if (timeout == 0) fail("No cmd bean's heard before timeout");
		}
		///...then check it's the right one.
		if (cmdBeans.get(cmdBeans.size()-1) instanceof PauseBean) {
			System.out.println("I finished after: "+timeout);
			PauseBean lastBean = (PauseBean)cmdBeans.get(cmdBeans.size()-1);
			assertEquals("PauseBean does not pause the job-queue consumer", mockCons.getConsumerId(), lastBean.getConsumerId());
		} else {
			fail("Last published bean was not a PauseBean");
		}
	}
	
	private void checkSubmittedBeans(MockSubmitter<QueueAtom> ms) throws Exception {
		String qName = tBeProcr.getAtomQueueProcessor().getActiveQueueID()+IQueue.SUBMISSION_QUEUE_SUFFIX;
		List<QueueAtom> submittedBeans = ms.getQueue(qName);
		assertTrue("No beans in the final status set", submittedBeans.size() != 0);
		for (QueueAtom dummy : submittedBeans) {
			//First check beans are in final state
			assertEquals("Final bean "+dummy.getName()+" is not submitted (was: "+dummy.getStatus()+")", Status.SUBMITTED ,dummy.getStatus());
			//Check the properties of the ScanAtom have been correctly passed down
			assertFalse("No beamline set", dummy.getBeamline() == null);
			assertEquals("Incorrect beamline", tBe.getBeamline(), dummy.getBeamline());
			assertFalse("No hostname set", dummy.getHostName() == null);
			assertEquals("Incorrect hostname", tBe.getHostName(), dummy.getHostName());
			assertFalse("No username set", dummy.getUserName() == null);
			assertEquals("Incorrect username", tBe.getUserName(), dummy.getUserName());
		}
	}

}
