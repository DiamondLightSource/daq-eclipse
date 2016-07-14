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
	protected IQueueProcessor<DummyAtom> getTestProcessor(boolean makeNew) {
		if (dAtProcr == null || makeNew) dAtProcr = new DummyAtomProcessor();
		return dAtProcr;
	}
	
	/**
	 * Extra test to check processing of other Dummy type
	 * @throws Exception
	 */
	@Test
	public void testDummyBeanExecution() throws Exception {
		dBe = new DummyBean("Lachesis", 200);
		dBeProcr = new DummyBeanProcessor();
		
		checkInitialBeanState(dBe);
		
		doExecute(dBeProcr, dBe);
		waitForExecutionEnd(10000l);
		
		checkLastBroadcastBeanStatuses(dBe, Status.COMPLETE, false);
	}
	
	/**
	 * Extra test to check processing of other Dummy type
	 * @throws Exception
	 */
	@Test
	public void testDummyHasQueueExecution() throws Exception {
		dHQ = new DummyHasQueue("Atropos", 300);
		dHQProcr = new DummyHasQueueProcessor();
		
		checkInitialBeanState(dHQ);
		
		doExecute(dHQProcr, dHQ);
		waitForExecutionEnd(10000l);
		
		checkLastBroadcastBeanStatuses(dHQ, Status.COMPLETE, false);
	}

	@Override
	protected void processorSpecificExecTests() throws Exception {
		//None	
	}

	@Override
	protected void processorSpecificTermTests() throws Exception {
		//None
	}

	@Override
	protected Queueable getFailBean() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void causeFail() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processorSpecificFailTests() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void waitToTerminate() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
