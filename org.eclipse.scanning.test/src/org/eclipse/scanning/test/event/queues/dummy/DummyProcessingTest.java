package org.eclipse.scanning.test.event.queues.dummy;

import static org.junit.Assert.assertEquals;

import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.test.event.queues.processors.AbstractQueueProcessorTest;
import org.junit.Test;

/*
 * TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!
 * Merge the generic tests in this with AbstractQueueProcessorTest 
 * (or replace & re-use as necessary)
 */

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
	public void testDummyAtomRunning() throws Exception {
		dAt = new DummyAtom("Clotho", 100);
		dAtProcr= new DummyAtomProcessor();
		
		assertEquals("Wrong initial status", Status.NONE, dAt.getStatus());
		assertEquals("Should not be non-zero percent complete", 0d, dAt.getPercentComplete(), 0);
		
		doExecute(dAtProcr, dAt);
		execLatch.await();
		
		assertEquals("Wrong final status", Status.COMPLETE, dAt.getStatus());
		assertEquals("Should be 100 percent complete at end", 100d, dAt.getPercentComplete(), 0);
	}
	
	@Test
	public void testDummyBeanRunning() throws Exception {
		dBe = new DummyBean("Lachesis", 200);
		dBeProcr = new DummyBeanProcessor();
		
		assertEquals("Wrong initial status", Status.NONE, dBe.getStatus());
		assertEquals("Should not be non-zero percent complete", 0d, dBe.getPercentComplete(), 0);
		
		doExecute(dBeProcr, dBe);
		execLatch.await();
		
		assertEquals("Wrong final status", Status.COMPLETE, dBe.getStatus());
		assertEquals("Should be 100 percent complete at end", 100d, dBe.getPercentComplete(), 0);
	}
	
	@Test
	public void testDummyHasQueueRunning() throws Exception {
		dHQ = new DummyHasQueue("Atropos", 300);
		dHQProcr = new DummyHasQueueProcessor();
		
		assertEquals("Wrong initial status", Status.NONE, dHQ.getStatus());
		assertEquals("Should not be non-zero percent complete", 0d, dHQ.getPercentComplete(), 0);
		
		doExecute(dHQProcr, dHQ);
		execLatch.await();
		
		assertEquals("Wrong final status", Status.COMPLETE, dHQ.getStatus());
		assertEquals("Should be 100 percent complete at end", 100d, dHQ.getPercentComplete(), 0);
	}

}
