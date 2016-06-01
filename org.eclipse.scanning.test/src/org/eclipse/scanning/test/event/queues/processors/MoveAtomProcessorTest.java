package org.eclipse.scanning.test.event.queues.processors;

import static org.junit.Assert.assertEquals;
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
		waitForExecutionEnd(10000);
		
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
		 */
		checkInitialBeanState(mvAt);
		doExecute(mvProcr, mvAt);
		Thread.sleep(120);
		qProc.terminate();
		waitForBeanFinalStatus(mvAt, 10000l);
		
		checkBroadcastBeanStatuses(mvAt, Status.TERMINATED, false);
		
		assertTrue("IPositioner not aborted", ((MockPositioner)mss.createPositioner()).isAborted());
	}
	

//	@Before
//	public void setup() throws Exception {
//		createStatusPublisher();
//		mvAt = TestAtomMaker.makeTestMoveAtomA();
//		processorSetup();
//	}
//	
//	private void processorSetup() throws Exception {
//		proc = new MoveAtomProcessor().makeProcessWithScanServ(mvAt, statPub, true, mss);
//	}
//	
//	@Test
//	public void testExecution() throws Exception {
//		doExecute();
//		
//		//In the MockPositioner, it takes 4.5secs to set the position
//		pauseForMockFinalStatus(10000);
//		
//		/*
//		 * After execution:
//		 * - first bean in statPub should be Status.RUNNING
//		 * - last bean in statPub should be Status.COMPLETE and 100%
//		 * - status publisher should have: 1 RUNNING bean and 1 COMPLETE bean
//		 * - the IPosition should be a MapPosition with map based on atom's map
//		 */
//		checkBeanFinalStatus(Status.COMPLETE);
//		
//		IPosition expected = new MapPosition(mvAt.getPositionConfig());
//		assertEquals("Position reported by scan service different from expected", expected, mss.createPositioner().getPosition());
//	}
//	
//	@Test
//	public void testTerminate() throws Exception {
//		doExecute();
//		
//		Thread.sleep(1000);
//		proc.terminate();
//		//Wait to allow the house to come crashing down
//		pauseForMockFinalStatus(10000);
//		/*
//		 * On terminate:
//		 * - first bean in statPub should be Status.RUNNING
//		 * - last bean in statPub should Status.TERMINATED and not be 100% complete
//		 * - status publisher should have a TERMINATED bean
//		 * - IPositioner should have received an abort command
//		 */
//		checkBeanFinalStatus(Status.TERMINATED);
//		
//		assertTrue("IPositioner not aborted", ((MockPositioner)mss.createPositioner()).isAborted());
//	}
//	
//	@Test
//	public void testErrorInMove() throws Exception {
//		//Redefine the move atom so it will cause an exception in the Mock
//		mvAt = new MoveAtom("Error Causer", "BadgerApocalypseButton", "pushed", 1);
//		processorSetup();
//		doExecute();
//		
//		pauseForMockFinalStatus(10000);
//		
//		/*
//		 * On exception:
//		 * - first bean in statPub should be Status.RUNNING
//		 * - last bean in statPub should be marked FAILED and not be 100% complete
//		 */
//		checkBeanFinalStatus(Status.FAILED);
//		assertEquals("Fail message from IPositioner incorrectly set", "Moving device(s) failed: The badger apocalypse cometh!", mvAt.getMessage());
//		
//	}

}
