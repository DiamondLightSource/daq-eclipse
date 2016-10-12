package org.eclipse.scanning.test.event.queues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.alive.ConsumerCommandBean;
import org.eclipse.scanning.api.event.alive.KillBean;
import org.eclipse.scanning.api.event.queues.IQueueControllerService;
import org.eclipse.scanning.event.queues.QueueControllerService;
import org.eclipse.scanning.event.queues.ServicesHolder;
import org.eclipse.scanning.test.event.queues.mocks.MockEventService;
import org.eclipse.scanning.test.event.queues.mocks.MockPublisher;
import org.eclipse.scanning.test.event.queues.mocks.MockQueueService;
import org.junit.Before;
import org.junit.Test;

public class QueueServiceControllerTest {
	
	private MockQueueService mockQServ;
	private MockPublisher<ConsumerCommandBean> mockCmdPub;
	private MockEventService mockEvServ = new MockEventService();
	
	@Before
	public void setUp() {
		//Configure the MockQueueService
		mockQServ = new MockQueueService();
		ServicesHolder.setQueueService(mockQServ);
		
		//Configure the MockEventService
		mockCmdPub = new MockPublisher<>(null, null);
		mockEvServ.setMockCmdPublisher(mockCmdPub);
		ServicesHolder.setEventService(mockEvServ);
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
	 * Test submission of 
	 */
	//TODO	@Test
	public void testSubmitRemove() {
		
	}
	
	//TODO	@Test
	public void testReorder() {
		
	}
	
	//TODO	@Test
	public void testPauseResume() {
		
	}
	
	//TODO	@Test
	public void testTerminate() {
		
	}
	
	//TODO	@Test
	public void testPauseResumeQueue() {
		
	}
	
	/**
	 * Test creation of KillBeans to kill queues.
	 * @throws EventException
	 */
	@Test
	public void testKillQueue() throws EventException {
		IQueueControllerService testController = new QueueControllerService();
		
		String jqID = mockQServ.getJobQueueID();
		String aqID = mockQServ.registerNewActiveQueue();
		assertFalse("job-queue consumer ID should not be null", mockQServ.getQueue(jqID).getConsumerID() == null);
		assertFalse("active-queue consumer ID should not be null", mockQServ.getQueue(aqID).getConsumerID() == null);
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
	 */
	private void analyseKiller(String queueID, boolean disconnect, boolean exitProc) {
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
