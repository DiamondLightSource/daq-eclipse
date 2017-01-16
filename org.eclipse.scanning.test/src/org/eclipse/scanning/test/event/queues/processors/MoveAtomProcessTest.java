package org.eclipse.scanning.test.event.queues.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.beans.MoveAtom;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.QueueProcess;
import org.eclipse.scanning.event.queues.ServicesHolder;
import org.eclipse.scanning.event.queues.processors.MoveAtomProcess;
import org.eclipse.scanning.test.event.queues.mocks.MockPositioner;
import org.eclipse.scanning.test.event.queues.mocks.MockScanService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MoveAtomProcessTest {
	
	private MoveAtom mvAt;
	private QueueProcess<MoveAtom, Queueable> mvAtProc;
	
	//Infrastructure
	private ProcessTestInfrastructure pti;
	private IRunnableDeviceService mss;
	
	@Before
	public void setUp() throws EventException {
		pti = new ProcessTestInfrastructure();
		
		mss = new MockScanService();
		ServicesHolder.setDeviceService(mss);
		
		mvAt = new MoveAtom("Move robot arm", "robot_arm", "1250", 12000);
		mvAtProc = new MoveAtomProcess<>(mvAt, pti.getPublisher(), false);
	}
	
	@After
	public void tearDown() {
		ServicesHolder.unsetDeviceService(mss);
		mss = null;
		
		pti = null;
	}
	
	/**
	 * After execution:
	 * - first bean in statPub should be Status.RUNNING
	 * - last bean in statPub should be Status.COMPLETE and 100%
	 * - status publisher should have: 1 RUNNING bean and 1 COMPLETE bean
	 * 
	 */
	@Test
	public void testExecution() throws Exception {
		pti.executeProcess(mvAtProc, mvAt);
		pti.waitForExecutionEnd(10000l);
		pti.checkLastBroadcastBeanStatuses(Status.COMPLETE, false);
		
		assertEquals("Incorrect message after execute", "Device move(s) completed.", pti.getLastBroadcastBean().getMessage());
	}
	
	/**
	 * On terminate:
	 * - first bean in statPub should be Status.RUNNING
	 * - last bean in statPub should Status.TERMINATED and not be 100% complete
	 * - status publisher should have a TERMINATED bean
	 * - IPositioner should have received an abort command
	 * 
	 * N.B. MoveAtomProcessorTest uses MockPostioner, which pauses for 100ms 
	 * does something then pauses for 150ms.
	 */
	@Test
	public void testTermination() throws Exception {
		pti.executeProcess(mvAtProc, mvAt);
		pti.waitToTerminate(100l);
		pti.waitForBeanFinalStatus(5000l);
		pti.checkLastBroadcastBeanStatuses(Status.TERMINATED, false);
		
		Thread.sleep(100);
		assertEquals("Incorrect message after terminate", "Move aborted before completion (requested).", pti.getLastBroadcastBean().getMessage());
		assertTrue("IPositioner not aborted", ((MockPositioner)mss.createPositioner()).isAborted());
		assertFalse("Move should have been terminated", ((MockPositioner)mss.createPositioner()).isMoveComplete());
	}
	
	/**
	 * On failure:
	 * - first bean in statPub should be Status.RUNNING
	 * - last bean in statPub should Status.FAILED and not be 100% complete
	 * - message with details should be set on bean
	 * - IPositioner should have received an abort command
	 */
	@Test
	public void testFailure() throws Exception {
		MoveAtom failAtom = new MoveAtom("Error Causer", "BadgerApocalypseButton", "pushed", 1);
		mvAtProc = new MoveAtomProcess<>(failAtom, pti.getPublisher(), false);
		
		pti.executeProcess(mvAtProc, failAtom);
		//Fail happens automatically since using MockDev.Serv.
		pti.waitForBeanFinalStatus(5000l);
		pti.checkLastBroadcastBeanStatuses(Status.FAILED, false);
		
		Queueable lastBean = pti.getLastBroadcastBean();
		assertEquals("Fail message from IPositioner incorrectly set", "Moving device(s) in '"+lastBean.getName()+
				"' failed: \"The badger apocalypse cometh! (EXPECTED - we pressed the button...)\".", lastBean.getMessage());
		assertTrue("IPositioner not aborted", ((MockPositioner)mss.createPositioner()).isAborted());
	}
	

}
