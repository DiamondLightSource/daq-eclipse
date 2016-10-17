package org.eclipse.scanning.test.event.queues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.alive.ConsumerCommandBean;
import org.eclipse.scanning.api.event.alive.KillBean;
import org.eclipse.scanning.api.event.alive.PauseBean;
import org.eclipse.scanning.api.event.queues.IQueueControllerService;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.queues.beans.QueueBean;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.QueueControllerService;
import org.eclipse.scanning.event.queues.ServicesHolder;
import org.eclipse.scanning.test.event.queues.dummy.DummyAtom;
import org.eclipse.scanning.test.event.queues.dummy.DummyBean;
import org.eclipse.scanning.test.event.queues.mocks.MockConsumer;
import org.eclipse.scanning.test.event.queues.mocks.MockEventService;
import org.eclipse.scanning.test.event.queues.mocks.MockPublisher;
import org.eclipse.scanning.test.event.queues.mocks.MockQueue;
import org.eclipse.scanning.test.event.queues.mocks.MockQueueService;
import org.eclipse.scanning.test.event.queues.mocks.MockSubmitter;
import org.junit.Before;
import org.junit.Test;

public class QueueControllerServiceTest {
	
	private MockQueueService mockQServ;
	private MockPublisher<ConsumerCommandBean> mockCmdPub;
	private MockPublisher<Queueable> mockPub;
	private MockSubmitter<Queueable> mockSub;
	private MockEventService mockEvServ;
	private String jqID, aqID;
	
	@Before
	public void setUp() throws EventException {
		//Configure the MockEventService
		mockEvServ = new MockEventService();
		mockPub = new MockPublisher<>(null, null);
		mockEvServ.setMockPublisher(mockPub);
		mockCmdPub = new MockPublisher<>(null, null);
		mockEvServ.setMockCmdPublisher(mockCmdPub);
		mockSub = new MockSubmitter<>();
		mockEvServ.setMockSubmitter(mockSub);
		ServicesHolder.setEventService(mockEvServ);
		
		//Configure the MockQueueService
		jqID = "mock-job-queue";
		aqID = "mock-active-queue";
		MockConsumer<QueueBean> mockJCons = new MockConsumer<>();
		MockQueue<QueueBean> mockJobQ = new MockQueue<>(jqID, mockJCons);
		MockConsumer<QueueAtom> mockACons = new MockConsumer<>();
		MockQueue<QueueAtom> mockActiveQ = new MockQueue<>(aqID, mockACons);
		mockQServ = new MockQueueService(mockJobQ, mockActiveQ);
		mockQServ.setCommandTopicName("mock-cmd-topic");
		//Clear queues to avoid class cast errors (a StatusBean is prepopulated for another test elsewhere...)
		mockQServ.getJobQueue().clearQueues();
		mockQServ.getActiveQueue(aqID).clearQueues();
		ServicesHolder.setQueueService(mockQServ);
		//Check that our job- and active-consumers do values which are different IDs
		assertFalse("job-queue consumer ID should not be null", mockQServ.getQueue(jqID).getConsumerID() == null);
		assertFalse("active-queue consumer ID should not be null", mockQServ.getQueue(aqID).getConsumerID() == null);
		assertFalse("job- & active-queue IDs identical", mockQServ.getQueue(jqID).getConsumerID() == mockQServ.getQueue(aqID).getConsumerID());
	}
		
	/**
	 * Test whether starting & stopping pushes the right buttons in the 
	 * QueueService
	 * @throws EventException 
	 */
	@Test
	public void testStartStop() throws EventException {
		IQueueControllerService testController = new QueueControllerService();
		
		testController.start();
		assertTrue("Start didn't push the start button.", mockQServ.isActive());
		
		testController.stop(false);
		assertFalse("Stop didn't push the stop button.", mockQServ.isActive());
		assertFalse("Stop should not have been forced.", mockQServ.isForced());
		
		testController.stop(true);
		assertTrue("Stop should have been forced.", mockQServ.isForced());
	}
	
	/**
	 * Test submission and removal of beans in queues. 
	 * @throws Exception 
	 */
	@Test
	public void testSubmitRemove() throws Exception {
		String submQ;
		
		//Beans for submission
		DummyBean albert = new DummyBean("Albert", 10),bernard = new DummyBean("Bernard", 20);
		DummyAtom carlos = new DummyAtom("Carlos", 30), duncan = new DummyAtom("Duncan", 40);
		DummyAtom xavier = new DummyAtom("Xavier", 100);
		
		IQueueControllerService testController = new QueueControllerService();
		/*
		 * Submit:
		 * - new DummyBean in job-queue with a particular name
		 * - 2nd and we have two in submit queue (second has particular name)
		 * - two DummyBeans in active-queue with particular names 
		 */
		//job-queue
		testController.submit(albert, jqID);
		submQ = mockQServ.getJobQueue().getSubmissionQueueName();
		assertEquals("No beans in job-queue after 1 submission", 1, mockSub.getQueueSize(submQ));
		assertEquals("Bean has wrong name", "Albert", mockSub.getLastSubmitted(submQ).getName());
		
		testController.submit(bernard, jqID);
		assertEquals("Bean has wrong name", "Bernard", mockSub.getLastSubmitted(submQ).getName());
		
		//active-queue
		submQ = mockQServ.getActiveQueue(aqID).getSubmissionQueueName();
		testController.submit(carlos, aqID);
		assertEquals("No beans in active-queue after 1 submission", 1, mockSub.getQueueSize(submQ));
		assertEquals("Bean has wrong name", "Carlos", mockSub.getLastSubmitted(submQ).getName());

		testController.submit(duncan, aqID);
		
		/*
		 * Remove:
		 * - now only one bean left and it job-queue (1st)
		 * - now only one bean left in active-queue (2nd)
		 * - throw an exception if the removed bean is no-longer removeable 
		 *   (or not in the queue in the first place)
		 */
		//job-queue
		testController.remove(bernard, jqID);
		submQ = mockQServ.getJobQueue().getSubmissionQueueName();
		assertEquals("Should only be one bean left in active-queue", 1, mockSub.getQueueSize(submQ));
		assertEquals("Wrong bean found in queue", "Albert", mockSub.getLastSubmitted(submQ).getName());
		
		//active-queue
		testController.remove(carlos, aqID);
		submQ = mockQServ.getActiveQueue(aqID).getSubmissionQueueName();
		assertEquals("Should only be one bean left in active-queue", 1, mockSub.getQueueSize(submQ));
		assertEquals("Wrong bean found in queue", "Duncan", mockSub.getLastSubmitted(submQ).getName());
		try {
			testController.remove(carlos, aqID);
			fail("Expected EventException: Carlos has already been removed");
		} catch (EventException evEx) {
			//Expected
		}
		try {
			testController.remove(xavier, aqID);
			fail("Expected EventException: Xavier was never submitted = can't be removed");
		} catch (EventException evEx) {
			//Expected
		}
		
		/*
		 * Prevent submission/removal of wrong bean type to wrong queue
		 */
		try {
			testController.submit(carlos, jqID);
			fail("Expected IllegalArgumentException when wrong queueable type submitted (atom in job-queue)");
		} catch (IllegalArgumentException iaEx) {
			//Expected
		}
		try {
			testController.submit(bernard, aqID);
			fail("Expected IllegalArgumentException when wrong queueable type submitted (bean in atom-queue)");
		} catch (IllegalArgumentException iaEx) {
			//Expected
		}
		try {
			testController.remove(bernard, aqID);
			fail("Expected IllegalArgumentException when wrong queueable type submitted (bean in atom-queue)");
		} catch (IllegalArgumentException iaEx) {
			//Expected
		}
	}
	
	/**
	 * Tests request of reordering processes in a queue
	 * @throws EventException
	 */
	@Test
	public void testReorder() throws EventException {				
		//Beans for submission
		DummyBean albert = new DummyBean("Albert", 10);
		DummyAtom carlos = new DummyAtom("Carlos", 30);
		DummyAtom xavier = new DummyAtom("Xavier", 100);
		
		IQueueControllerService testController = new QueueControllerService();
		//Add two beans to the queues
		testController.submit(albert, jqID);
		testController.submit(carlos, aqID);		
		/*
		 * Submit beans & reorder
		 * - check number of moves correct for given bean
		 */
		assertFalse("Albert indicated reordered, but no reordering done", mockSub.isBeanReordered(albert));
		assertFalse("Carlos indicated reordered, but no reordering done", mockSub.isBeanReordered(carlos));
		testController.reorder(albert, 4, jqID);
		assertTrue("Albert not reordered after reordering done", mockSub.isBeanReordered(albert));
		assertEquals("Incorrect number of moves after reordering", 4, mockSub.getReorderedBeanMove(albert));
		assertFalse("Carlos indicated reordered, but no reordering done", mockSub.isBeanReordered(carlos));
		testController.reorder(carlos, 3, aqID);
		assertTrue("Carlos not reordered after reordering done", mockSub.isBeanReordered(carlos));
		assertEquals("Incorrect number of moves after reordering", 3, mockSub.getReorderedBeanMove(carlos));
		
		/*
		 * Check EventException thrown when bean not present.
		 */
		try {
			testController.reorder(xavier, 3, aqID);
			fail("Expected EventException: Xavier not submitted, should not be able to reorder");
		} catch (EventException evEx) {
			//Expected
		}
		try {
			testController.reorder(carlos, 2, jqID);
			fail("Expected IllegalArgumentException when wrong queueable type reordered (atom in job-queue)");
		} catch (IllegalArgumentException iaEx) {
			//Expected
		}
		try {
			testController.submit(albert, aqID);
			fail("Expected IllegalArgumentException when wrong queueable type reordered (bean in atom-queue)");
		} catch (IllegalArgumentException iaEx) {
			//Expected
		}
		
	}
	
	/**
	 * Tests requests for pausing/resumption of processes within a queue.
	 * @throws EventException
	 */
	@Test
	public void testPauseResume() throws EventException {
		//Beans for submission
		DummyBean albert = new DummyBean("Albert", 10);
		DummyAtom carlos = new DummyAtom("Carlos", 30);
		DummyAtom xavier = new DummyAtom("Xavier", 100);
		
		//Set up beans in right queues
		setUpTwoBeanStatusSet(albert, carlos);
		
		IQueueControllerService testController = new QueueControllerService();
		/*
		 * Pause process in job-queue
		 * - check published bean has REQUEST_PAUSE status
		 * - check EventException thrown when bean with isPaused status is paused   
		 */
		testController.pause(albert, jqID);
		assertEquals("Published bean has wrong Status", Status.REQUEST_PAUSE, mockPub.getLastQueueable().getStatus());
		try {
			testController.pause(albert, jqID);
			fail("Expected EventException on repeated pause");
		} catch (EventException evEx) {
			//Expected
		}
		
		/*
		 * Resume process in job-queue
		 * - check published bean has REQUEST_RESUME status
		 * - check EventException thrown when bean with isResumed/isRunning status is resumed
		 */
		testController.resume(albert, jqID);
		assertEquals("Published bean has wrong Status", Status.REQUEST_RESUME, mockPub.getLastQueueable().getStatus());
		try {
			testController.resume(albert, jqID);
			fail("Expected EventException on repeated resume");
		} catch (EventException evEx) {
			//Expected
		}
		
		/*
		 * Pause process in active-queue
		 * - check published bean has REQUEST_PAUSE status 
		 */
		testController.pause(carlos, aqID);
		assertEquals("Published bean has wrong Status", Status.REQUEST_PAUSE, mockPub.getLastQueueable().getStatus());
		
		/*
		 * Resume process in active-queue
		 * - check published bean has REQUEST_RESUME status
		 */
		testController.resume(carlos, aqID);
		assertEquals("Published bean has wrong Status", Status.REQUEST_RESUME, mockPub.getLastQueueable().getStatus());
		
		/*
		 * Test wrong bean type to wrong queue & non-existent paused/resumed bean
		 */
		try {
			testController.pause(carlos, jqID);
			fail("Expected IllegalArgumentException when wrong queueable type paused (atom in job-queue)");
		} catch (IllegalArgumentException iaEx) {
			//Expected
		}
		try {
			testController.resume(carlos, jqID);
			fail("Expected IllegalArgumentException when wrong queueable type resumed (atom in job-queue)");
		} catch (IllegalArgumentException iaEx) {
			//Expected
		}
		try {
			testController.pause(albert, aqID);
			fail("Expected IllegalArgumentException when wrong queueable type paused (bean in active-queue)");
		} catch (IllegalArgumentException iaEx) {
			//Expected
		}
		try {
			testController.resume(albert, aqID);
			fail("Expected IllegalArgumentException when wrong queueable type resumed (bean in active-queue)");
		} catch (IllegalArgumentException iaEx) {
			//Expected
		}
		try {
			testController.pause(xavier, aqID);
			fail("Expected EventException: Xavier not submitted, should not be able to pause");
		} catch (EventException evEx) {
			//Expected
		}
		try {
			testController.resume(xavier, aqID);
			fail("Expected EventException: Xavier not submitted, should not be able to resume");
		} catch (EventException evEx) {
			//Expected
		}
	}
	
	/**
	 * Tests requesting termination of processes within a queue.
	 * @throws EventException
	 */
	@Test
	public void testTerminate() throws EventException {
		//Beans for submission
		DummyBean albert = new DummyBean("Albert", 10);
		DummyAtom carlos = new DummyAtom("Carlos", 30);
		DummyAtom xavier = new DummyAtom("Xavier", 100);
		
		//Set up beans in right queues
		setUpTwoBeanStatusSet(albert, carlos);

		IQueueControllerService testController = new QueueControllerService();
		/*
		 * Pause process in job-queue
		 * - check published bean has REQUEST_PAUSE status
		 * - check EventException thrown when bean with isPaused status is paused   
		 */
		testController.terminate(albert, jqID);
		assertEquals("Published bean has wrong Status", Status.REQUEST_TERMINATE, mockPub.getLastQueueable().getStatus());
		try {
			testController.terminate(albert, jqID);
			fail("Expected EventException on repeated terminate");
		} catch (EventException evEx) {
			//Expected
		}

		/*
		 * Pause process in active-queue
		 * - check published bean has REQUEST_PAUSE status 
		 */
		testController.terminate(carlos, aqID);
		assertEquals("Published bean has wrong Status", Status.REQUEST_TERMINATE, mockPub.getLastQueueable().getStatus());

		/*
		 * Test wrong bean type to wrong queue & non-existent terminated bean
		 */
		try {
			testController.terminate(carlos, jqID);
			fail("Expected IllegalArgumentException when wrong queueable type terminated (atom in job-queue)");
		} catch (IllegalArgumentException iaEx) {
			//Expected
		}
		try {
			testController.terminate(albert, aqID);
			fail("Expected IllegalArgumentException when wrong queueable type terminated (bean in active-queue)");
		} catch (IllegalArgumentException iaEx) {
			//Expected
		}
		try {
			testController.terminate(xavier, aqID);
			fail("Expected EventException: Xavier not submitted, should not be able to terminate");
		} catch (EventException evEx) {
			//Expected
		}		
	}
	
	private void setUpTwoBeanStatusSet(QueueBean albert, QueueAtom carlos) throws EventException {		
		//Set up beans in right queues
		MockConsumer<QueueBean> jCons = (MockConsumer<QueueBean>) mockQServ.getJobQueue().getConsumer();
		jCons.addToStatusSet(albert);
		MockConsumer<QueueAtom> aCons = (MockConsumer<QueueAtom>) mockQServ.getActiveQueue(aqID).getConsumer();
		aCons.addToStatusSet(carlos);
	}
	
	/**
	 * Test configuration & publishing of PauseBeans to pause/resume whole queues.
	 * @throws EventException
	 */
	@Test
	public void testPauseResumeQueue() throws EventException {
		IQueueControllerService testController = new QueueControllerService();
		
		/*
		 * Pause the job-queue
		 * - check PauseBean sent
		 * - check has correct consumerID
		 * Unpause the job-queue
		 * - check PauseBean sent
		 * - check has correct consumerID
		 */
		testController.pauseQueue(jqID);
		analysePauser(jqID, true);
		testController.resumeQueue(jqID);
		analysePauser(jqID, false);
		
		/*
		 * Pause the active-queue
		 * - check PauseBean sent
		 * - check has correct consumerID
		 * Unpause the active-queue
		 * - check PauseBean sent
		 * - check has correct consumerID
		 */
		testController.pauseQueue(aqID);
		analysePauser(aqID, true);
		testController.resumeQueue(aqID);
		analysePauser(aqID, false);
	}
	
	private void analysePauser(String queueID, boolean pause) throws EventException {
		if (mockCmdPub.getLastCmdBean() instanceof PauseBean) {
			PauseBean pauser = (PauseBean)mockCmdPub.getLastCmdBean();
			assertEquals("Kill bean has wrong consumer ID", mockQServ.getQueue(queueID).getConsumerID(), pauser.getConsumerId());
			assertEquals("Wrong boolean for pause field", pause, pauser.isPause());
		} else {
			fail("Last command bean was not a PauseBean.");
		}
	}
	
	/**
	 * Test configuration & publishing of KillBeans to kill queues.
	 * @throws EventException
	 */
	@Test	
	public void testKillQueue() throws EventException {
		IQueueControllerService testController = new QueueControllerService();
		
		/*
		 * Kill the job-queue
		 * - check KillBean sent
		 * - check KillBean has correct consumerID
		 * - check options passed through correctly
		 */
		testController.killQueue(jqID, true, false);
		analyseKiller(jqID, true, false);
		
		/*
		 * Kill active-queue
		 * - check KillBean sent
		 * - check KillBean has correct consumerID
		 * - check options passed through correctly
		 */
		testController.killQueue(aqID, false, true);
		analyseKiller(aqID, false, true);
	}
	
	/**
	 * Checks KillBean configured as expected
	 * @param queueID
	 * @throws EventException 
	 */
	private void analyseKiller(String queueID, boolean disconnect, boolean exitProc) throws EventException {
		if (mockCmdPub.getLastCmdBean() instanceof KillBean) {
			KillBean killer = (KillBean)mockCmdPub.getLastCmdBean();
			assertEquals("Kill bean has wrong consumer ID", mockQServ.getQueue(queueID).getConsumerID(), killer.getConsumerId());
			assertEquals("Disconnect should be true (was false)", disconnect, killer.isDisconnect());
			assertEquals("ExitProcess should be false (was true)", exitProc, killer.isExitProcess());
		} else {
			fail("Last command bean was not a KillBean.");
		}
	}

}
