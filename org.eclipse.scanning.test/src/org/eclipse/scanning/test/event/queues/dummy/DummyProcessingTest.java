package org.eclipse.scanning.test.event.queues.dummy;

import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.test.event.queues.processors.AbstractQueueProcessorTest;
import org.junit.Test;

/**
 * Tests ability of DummyProcessor to handle all three Dummy bean types.
 * Also tests generic behaviour of processor in terms of changing its 
 * configuration and possible options for its configuration.
 * 
 * @author Michael Wharmby
 */
public class DummyProcessingTest extends AbstractQueueProcessorTest {

	private DummyAtom dAt;
	private DummyBean dBe;
	private DummyHasQueue dHQ;
	
	private DummyAtomProcessor dAtProcr;
	private DummyBeanProcessor dBeProcr;
	private DummyHasQueueProcessor dHQProcr;
	
	@Override
	protected void localSetup() {
		//Do nothing
	}
	
	@Override
	protected void localTearDown() {
		dAt = null;
		dBe = null;
		dHQ = null;
		
		dAtProcr = null;
		dBeProcr = null;
		dHQProcr = null;
	}
	
	@Override
	protected Queueable getTestBean() {
		return new DummyAtom("Hera", 400);
	}
	@Override
	protected IQueueProcessor<DummyAtom> getTestProcessor() {
		return new DummyAtomProcessor();
	}
	
	@Test
	public void testDummyAtomExecution() throws Exception {
		dAt = new DummyAtom("Clotho", 100);
		dAtProcr= new DummyAtomProcessor();
		
		checkInitialBeanState(dAt);
		
		doExecute(dAtProcr, dAt);
		waitForExecutionEnd(10000l);
		
		checkLastBroadcastBeanStatuses(dAt, Status.COMPLETE, false);
	}
	
	@Test
	public void testDummyBeanExecution() throws Exception {
		dBe = new DummyBean("Lachesis", 200);
		dBeProcr = new DummyBeanProcessor();
		
		checkInitialBeanState(dBe);
		
		doExecute(dBeProcr, dBe);
		waitForExecutionEnd(10000l);
		
		checkLastBroadcastBeanStatuses(dBe, Status.COMPLETE, false);
	}
	
	@Test
	public void testDummyHasQueueExecution() throws Exception {
		dHQ = new DummyHasQueue("Atropos", 300);
		dHQProcr = new DummyHasQueueProcessor();
		
		checkInitialBeanState(dHQ);
		
		doExecute(dHQProcr, dHQ);
		waitForExecutionEnd(10000l);
		
		checkLastBroadcastBeanStatuses(dHQ, Status.COMPLETE, false);
	}

}
