package org.eclipse.scanning.test.event.queues.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.event.queues.QueueServicesHolder;
import org.eclipse.scanning.event.queues.beans.MoveAtom;
import org.eclipse.scanning.event.queues.processors.MoveAtomProcessor;
import org.eclipse.scanning.test.event.queues.mocks.MockPositioner;
import org.eclipse.scanning.test.event.queues.mocks.MockScanService;
import org.junit.Test;

public class MoveAtomProcessorTest extends AbstractQueueProcessorTest {
	
	private MoveAtom mvAt;
	private MoveAtomProcessor mvProcr;
	
	//Infrastructure
	private IRunnableDeviceService mss;

	@Override
	protected void localSetup() {
		mss = new MockScanService();
		QueueServicesHolder.setDeviceService(mss);
	}

	@Override
	protected void localTearDown() {
		QueueServicesHolder.unsetDeviceService(mss);
	}

	@Override
	protected IQueueProcessor<? extends Queueable> getTestProcessor() {
		return new MoveAtomProcessor();
	}

	@Override
	protected Queueable getTestBean() {
		return new MoveAtom("Move robot arm", "robot_arm", "1250", 12000);
	}
	
	@Test
	public void testMoveProcessorExecution() throws Exception {
		Map<String, Object> mvConf = new HashMap<>();
		mvConf.put("ocs", 80);
		mvConf.put("ocs_xPos", 250);
		
		mvAt = new MoveAtom("Prepare cryostream", mvConf, 30000);
		mvProcr = new MoveAtomProcessor();
		
		/*
		 * After execution:
		 * - first bean in statPub should be Status.RUNNING
		 * - last bean in statPub should be Status.COMPLETE and 100%
		 * - status publisher should have: 1 RUNNING bean and 1 COMPLETE bean
		 * - the IPosition should be a MapPosition with map based on atom's map
		 */
		checkInitialBeanState(mvAt);
		doExecute(mvProcr, mvAt);
		waitForExecutionEnd(10000l);
		
		checkBroadcastBeanStatuses(mvAt, Status.COMPLETE, false);
		
		IPosition expected = new MapPosition(mvAt.getPositionConfig());
		assertEquals("Position reported by scan service different from expected", expected, mss.createPositioner().getPosition());
	}
	
	@Test
	public void testMoveProcessorTermination() throws Exception {
		Map<String, Object> mvConf = new HashMap<>();
		mvConf.put("mDAC", 1.5);
		mvConf.put("pinhole", 3);
		
		mvAt = new MoveAtom("Change DAC pressure", mvConf, 30000);
		mvProcr = new MoveAtomProcessor();
		
		/*
		 * On terminate:
		 * - first bean in statPub should be Status.RUNNING
		 * - last bean in statPub should Status.TERMINATED and not be 100% complete
		 * - status publisher should have a TERMINATED bean
		 * - IPositioner should have received an abort command
		 * 
		 * (setPosition in MockPositioner pauses for 400ms, does something then pauses 
		 * for 450ms. If we sleep for 400ms, do some checking and then sleep for another 
		 * 600ms, any running setPosition calls should be done.)
		 */
		checkInitialBeanState(mvAt);
		doExecute(mvProcr, mvAt);
		Thread.sleep(400);
		qProc.terminate();
		waitForBeanFinalStatus(mvAt, 10000l);
		
		checkBroadcastBeanStatuses(mvAt, Status.TERMINATED, false);
		
		//Wait until any running 
		Thread.sleep(600);
		assertTrue("IPositioner not aborted", ((MockPositioner)mss.createPositioner()).isAborted());
		assertFalse("Move should have been terminated", ((MockPositioner)mss.createPositioner()).isMoveComplete());
	}
	
	@Test
	public void testErrorInMove() throws Exception {
		//Define the move atom so it will cause an exception in the Mock (there's a trigger)
		mvAt = new MoveAtom("Error Causer", "BadgerApocalypseButton", "pushed", 1);
		mvProcr = new MoveAtomProcessor();
		
		/*
		 * On exception:
		 * - first bean in statPub should be Status.RUNNING
		 * - last bean in statPub should be marked FAILED and not be 100% complete
		 */
		checkInitialBeanState(mvAt);
		doExecute(mvProcr, mvAt);
		waitForBeanFinalStatus(mvAt, 10000l);
		
		checkBroadcastBeanStatuses(mvAt, Status.FAILED, false);
		assertEquals("Fail message from IPositioner incorrectly set", "Moving device(s) in '"+mvAt.getName()+
				"' failed: \"The badger apocalypse cometh! (EXPECTED - we pressed the button...)\"", mvAt.getMessage());
	}

}
