package org.eclipse.scanning.test.event.queues.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.event.queues.beans.MoveAtom;
import org.eclipse.scanning.event.queues.processors.MoveAtomProcessor;
import org.eclipse.scanning.test.event.queues.beans.util.TestAtomMaker;
import org.eclipse.scanning.test.event.queues.mocks.MockPositioner;
import org.eclipse.scanning.test.event.queues.mocks.MockScanService;
import org.junit.Before;
import org.junit.Test;

public class MoveAtomProcessorTest extends AbstractQueueProcessorTest<QueueAtom> {
	
	private MoveAtom mvAt;
		
	private final MockScanService mss = new MockScanService();
	
	@Before
	public void setup() throws Exception {
		createStatusPublisher();
		mvAt = TestAtomMaker.makeTestMoveAtomA();
		processorSetup();
	}
	
	private void processorSetup() throws Exception {
		proc = new MoveAtomProcessor().makeProcessWithScanServ(mvAt, statPub, true, mss);
	}
	
	@Test
	public void testExecution() throws Exception {
		doExecute();
		
		//In the MockPositioner, it takes 4.5secs to set the position
		Thread.sleep(5500);
		
		/*
		 * After execution:
		 * - first bean in statPub should be Status.RUNNING
		 * - last bean in statPub should be Status.COMPLETE and 100%
		 * - status publisher should have: 1 RUNNING bean and 1 COMPLETE bean
		 * - the IPosition should be a MapPosition with map based on atom's map
		 */
		checkBeanFinalStatus(Status.COMPLETE);
		
		IPosition expected = new MapPosition(mvAt.getPositionConfig());
		assertEquals("Position reported by scan service different from expected", expected, mss.createPositioner().getPosition());
	}
	
	@Test
	public void testTerminate() throws Exception {
		doExecute();
		
		Thread.sleep(1000);
		proc.terminate();
		Thread.sleep(700);
		//There's a race condition due to the while loop checking only every 500secs
		/*
		 * On terminate:
		 * - first bean in statPub should be Status.RUNNING
		 * - last bean in statPub should Status.TERMINATED and not be 100% complete
		 * - status publisher should have a TERMINATED bean
		 * - IPositioner should have received an abort command
		 */
		checkBeanFinalStatus(Status.TERMINATED);
		
		assertTrue("IPositioner not aborted", ((MockPositioner)mss.createPositioner()).isAborted());
	}
	
	@Test
	public void testErrorInMove() throws Exception {
		//Redefine the move atom so it will cause an exception in the Mock
		mvAt = new MoveAtom("Error Causer", "BadgerApocalypseButton", "pushed", 1);
		processorSetup();
		doExecute();
		
		Thread.sleep(3000);
		
		/*
		 * On exception:
		 * - first bean in statPub should be Status.RUNNING
		 * - last bean in statPub should be marked FAILED and not be 100% complete
		 */
		checkBeanFinalStatus(Status.FAILED);
		assertEquals("Fail message from IPositioner incorrectly set", "Moving device(s) failed: The badger apocalypse cometh!", mvAt.getMessage());
		
	}

}
