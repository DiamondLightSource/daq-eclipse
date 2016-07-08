package org.eclipse.scanning.test.event.queues.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.event.queues.QueueServicesHolder;
import org.eclipse.scanning.event.queues.beans.MoveAtom;
import org.eclipse.scanning.event.queues.processors.MoveAtomProcessor;
import org.eclipse.scanning.test.event.queues.mocks.MockPositioner;
import org.eclipse.scanning.test.event.queues.mocks.MockPublisher;
import org.eclipse.scanning.test.event.queues.mocks.MockScanService;

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
	
	@Override
	protected Queueable getFailBean() {
		return new MoveAtom("Error Causer", "BadgerApocalypseButton", "pushed", 1);
	}
	
	@Override
	protected void causeFail() throws Exception {
		//Nothing to do, since using MockPositioner with fail simulation included.
	}
	
	@Override
	protected void processorSpecificExecTests() throws Exception {
		/*
		 * After execution:
		 * - the IPosition should be a MapPosition with map based on atom's map
		 */
		IPosition expected = new MapPosition(mvAt.getPositionConfig());
		assertEquals("Position reported by scan service different from expected", expected, mss.createPositioner().getPosition());
	}
	
	@Override
	protected void processorSpecificTermTests() throws Exception {
		/*
		 * After terminate:
		 * - IPositioner should have received an abort command
		 */
		assertTrue("IPositioner not aborted", ((MockPositioner)mss.createPositioner()).isAborted());
		assertFalse("Move should have been terminated", ((MockPositioner)mss.createPositioner()).isMoveComplete());
	}
	
	@Override
	protected void processorSpecificFailTests() throws Exception {
		/*
		 * After fail:
		 * - message with details should be set on bean
		 * - IPositioner should have received an abort command
		 */
		Queueable lastBean = ((MockPublisher<Queueable>)statPub).getLastBean();
		assertEquals("Fail message from IPositioner incorrectly set", "Moving device(s) in '"+lastBean.getName()+
				"' failed: \"The badger apocalypse cometh! (EXPECTED - we pressed the button...)\".", lastBean.getMessage());
		assertTrue("IPositioner not aborted", ((MockPositioner)mss.createPositioner()).isAborted());
	}
}
