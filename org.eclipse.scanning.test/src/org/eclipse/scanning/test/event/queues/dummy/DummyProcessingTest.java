package org.eclipse.scanning.test.event.queues.dummy;

import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.test.event.queues.processors.ProcessTestInfrastructure;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests ability of DummyProcessor to handle all three Dummy bean types.
 * Also tests generic behaviour of processor in terms of changing its 
 * configuration and possible options for its configuration.
 * 
 * @author Michael Wharmby
 */
public class DummyProcessingTest {
	
	private ProcessTestInfrastructure pti;
	
	@Before
	public void setup() {
		pti = new ProcessTestInfrastructure();
	}
	
	@After
	public void tearDown() {
		pti = null;
	}
	
	@Test
	public void testDummyAtomExecution() throws Exception {
		DummyAtom dAt = new DummyAtom("Clotho", 400);
		DummyAtomProcess<Queueable> dAtProc = new DummyAtomProcess<>(dAt, pti.getPublisher(), false);
		
		pti.executeProcess(dAtProc, dAt);
		pti.waitForExecutionEnd(10000L);
		pti.checkLastBroadcastBeanStatuses(dAt, Status.COMPLETE, false);
	}
	
	@Test
	public void testDummyBeanExecution() throws Exception {
		DummyBean dBe = new DummyBean("Lachesis", 200);
		DummyBeanProcess<Queueable> dBeProc = new DummyBeanProcess<>(dBe, pti.getPublisher(), false);
		
		pti.executeProcess(dBeProc, dBe);
		pti.waitForExecutionEnd(10000L);
		pti.checkLastBroadcastBeanStatuses(dBe, Status.COMPLETE, false);
	}
	
	@Test
	public void testDummyHasQueueExecution() throws Exception {
		DummyHasQueue dHQ = new DummyHasQueue("Atropos", 300);
		DummyHasQueueProcess<Queueable> dHQProc = new DummyHasQueueProcess<>(dHQ, pti.getPublisher(), false);
		
		pti.executeProcess(dHQProc, dHQ);
		pti.waitForExecutionEnd(10000L);
		pti.checkLastBroadcastBeanStatuses(dHQ, Status.COMPLETE, false);
	}
	
	/*
	 * Rest of the test is only for DummyAtoms since the process class is the same
	 */
	@Test
	public void testTermination() throws Exception {
		DummyAtom dAt = new DummyAtom("Hera", 400);
		DummyAtomProcess<Queueable> dAtProc = new DummyAtomProcess<>(dAt, pti.getPublisher(), false);
		
		pti.executeProcess(dAtProc, dAt);
		pti.waitToTerminate(10l);
		pti.waitForBeanFinalStatus(dAt, 5000l);
		pti.checkLastBroadcastBeanStatuses(dAt, Status.TERMINATED, false);
	}
	
	
	
}
