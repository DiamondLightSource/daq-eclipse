package org.eclipse.scanning.test.event.queues.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.eclipse.scanning.api.event.alive.ConsumerCommandBean;
import org.eclipse.scanning.api.event.alive.PauseBean;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.queues.beans.QueueBean;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.ServicesHolder;
import org.eclipse.scanning.event.queues.beans.SubTaskAtom;
import org.eclipse.scanning.event.queues.beans.TaskBean;
import org.eclipse.scanning.event.queues.processors.TaskBeanProcessor;
import org.eclipse.scanning.test.event.queues.mocks.MockConsumer;
import org.eclipse.scanning.test.event.queues.mocks.MockEventService;
import org.eclipse.scanning.test.event.queues.mocks.MockPublisher;
import org.eclipse.scanning.test.event.queues.mocks.MockQueue;
import org.eclipse.scanning.test.event.queues.mocks.MockQueueService;
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
	
	private static MockQueueService mockQServ;
	private static MockSubmitter<QueueAtom> mockSub;
	private static MockEventService mockEvServ;
	private static MockPublisher<QueueAtom> mockPub;
	private static MockPublisher<ConsumerCommandBean> mockCmdPub;
	private static MockConsumer<QueueBean> mockCons;
	private static MockQueue<QueueBean> mockJobQ;
	
	@BeforeClass
	public static void setUpClass() {
		//Configure the processor Mock queue infrastructure
		mockCons = new MockConsumer<>();
		mockJobQ = new MockQueue<>("mock-job-queue", mockCons);
		
		mockSub = new MockSubmitter<>();
		mockQServ = new MockQueueService(mockJobQ);
		mockQServ.setMockSubmitter(mockSub);
		ServicesHolder.setQueueService(mockQServ);
		
		mockPub = new MockPublisher<>(null,  null);
		mockCmdPub = new MockPublisher<>(null, null);
		mockEvServ = new MockEventService();
		mockEvServ.setMockPublisher(mockPub);
		mockEvServ.setMockCmdPublisher(mockCmdPub);
		ServicesHolder.setEventService(mockEvServ);
	}
	
	@AfterClass
	public static void tearDownClass() {
		ServicesHolder.unsetEventService(mockEvServ);
		mockEvServ = null;
		mockPub = null;
		
		ServicesHolder.unsetQueueService(mockQServ);
		mockQServ = null;
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
		tBe.queue().add(atomA);
		tBe.queue().add(atomB);
		tBe.queue().add(atomC);
		
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
		if (cmdBeans.get(cmdBeans.size()-1) instanceof PauseBean) {
			PauseBean lastBean = (PauseBean)cmdBeans.get(cmdBeans.size()-1);
			assertEquals("PauseBean does not pause the job-queue consumer", mockCons.getConsumerId(), lastBean.getConsumerId());
		} else {
			fail("Last published bean was not a PauseBean");
		}
	}
	
	protected void checkSubmittedBeans(MockSubmitter<QueueAtom> ms) throws Exception {
		List<QueueAtom> submittedBeans = ms.getQueue();
		assertTrue("No beans in the final status set", submittedBeans.size() != 0);
		for (QueueAtom dummy : submittedBeans) {
			//First check beans are in final state
			assertTrue("Final bean "+dummy.getName()+" is not final",dummy.getStatus().isFinal());
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
