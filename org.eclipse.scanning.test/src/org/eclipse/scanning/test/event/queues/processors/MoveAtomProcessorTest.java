package org.eclipse.scanning.test.event.queues.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
		mvProcr = null;
		mvAt = null;
	}

	@Override
	protected IQueueProcessor<? extends Queueable> getTestProcessor(boolean makeNew) {
		if (mvProcr == null || makeNew) mvProcr = new MoveAtomProcessor();
		return mvProcr;
	}

	@Override
	protected Queueable getTestBean() {
		if (mvAt == null) mvAt = new MoveAtom("Move robot arm", "robot_arm", "1250", 12000);
		return mvAt;
	}
	
	protected void processorSpecificExecTests() throws Exception {
		IPosition expected = new MapPosition(mvAt.getPositionConfig());
		assertEquals("Position reported by scan service different from expected", expected, mss.createPositioner().getPosition());
	};
	
	protected void processorSpecificTermTests() throws Exception {
		assertTrue("IPositioner not aborted", ((MockPositioner)mss.createPositioner()).isAborted());
		assertFalse("Move should have been terminated", ((MockPositioner)mss.createPositioner()).isMoveComplete());
	};
	
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
				"' failed: \"The badger apocalypse cometh! (EXPECTED - we pressed the button...)\".", mvAt.getMessage());
	}

}
