package org.eclipse.scanning.test.event.queues.dummy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.IQueueProcess;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.QueueProcess;
import org.eclipse.scanning.test.event.queues.mocks.MockPublisher;
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

	//Surrounding infrastructure config
	private IQueueProcess<Queueable> qProc;
	private IPublisher<Queueable> pub = new MockPublisher<Queueable>(null, null);

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
	
	/**
	 * Tests that once setExecuted or execute called, configuration of 
	 * processor cannot be changed.
	 * @throws Exception
	 */
	@Test
	public void testChangingProcessorAfterExecution() throws Exception {
		dAt = new DummyAtom("Clotho", 100);
		dAtProcr= new DummyAtomProcessor();
		try {
			changeBeanAfterExecution(dAtProcr, dAt);
			fail("Should not be able to change bean after execution start");
		} catch (EventException eEx) {
			//Expected
		}
		try {
			changeProcessAfterExecution(dAtProcr);
			fail("Should not be able to change bean after execution start");
		} catch (EventException eEx) {
			//Expected
		}
		
		//Try for real (create fresh Atom processor first)
		dAtProcr = new DummyAtomProcessor();
		assertFalse("Executed should initially be false", dAtProcr.isExecuted());
		
		//Execute, but don't wait for completion (no point)
		doExecute(dAtProcr, dAt);
		waitForBeanStatus(dAt, Status.RUNNING, 1000l);
		
		//Thread.sleep(100); //Because it takes time for the thread to start
		assertTrue("Executed should false after start", dAtProcr.isExecuted());
		

		try {
			dAtProcr.setProcessBean(dAt);
			fail("Should not be able to set bean after execution start");
		} catch (EventException eEx) {
			//Expected
		}
		try {
			dAtProcr.setQueueProcess(qProc);
			fail("Should not be able to set process after execution start");
		} catch (EventException eEx) {
			//Expected
		}
	}
	
	/**
	 * Test that type checking of the setProcessBean method prevents the wrong 
	 * bean being accepted by the processor.
	 * @throws Exception
	 */
//	@Test
	public void testWrongBeanType() throws Exception {
		DummyAtom dAtA = new DummyAtom("Hera", 500);
		DummyBean dAtB = new DummyBean("Hephaestus", 500);
		dAtProcr = new DummyAtomProcessor();
		qProc = new QueueProcess<Queueable>(dAtA, pub, true);
		
		//Configure the processor & process
		try {
			dAtProcr.setProcessBean(dAtB);
			fail("Should not be able to supply wrong bean type");
		} catch (EventException eEx) {
			//Expected
		}
	}
	
	/**
	 * Prevents a processor with bean type A set on it being passed to a 
	 * process with bean type B. I think this only comes about when beans are 
	 * passed as Queueables. 
	 * @throws Exception
	 */
//	@Test
	public void testDifferentBeanTypes() throws Exception {
		DummyAtom dAtA = new DummyAtom("Hera", 500);
		DummyBean dBeA = new DummyBean("Hephaestus", 500);
		dBeA.setLatch(execLatch);
		dBeProcr = new DummyBeanProcessor();
		
		try {
			runTest(dBeProcr, dBeA, dAtA, true);
			fail("Should not be able to execute with different bean types on processor & process");
		} catch (EventException eEx) {
			//Expected
		}
	}
	
	
	/**
	 * Normal way of running the test, where both QueueProcess and 
	 * QueueProcessor have same bean type (i.e. this is correct).
	 */
	private void runTest(IQueueProcessor<? extends Queueable> qProcr, Queueable beAt) throws Exception {
		runTest(qProcr, beAt, beAt, false);
	}
	
	/**
	 * Abnormal way of running test. QueueProcess & QueueProcessor can have 
	 * different bean types - this should never happen for real, but there's a 
	 * test in the code to make sure!
	 */
	private void runTest(IQueueProcessor<? extends Queueable> qProcr, Queueable beAtA, Queueable beAtB, boolean earlyStop) throws Exception {
		qProc = new QueueProcess<Queueable>(beAtB, pub, true);
		
		//Configure the processor & process
		qProcr.setQueueProcess(qProc);
		qProcr.setProcessBean(beAtA);
		qProc.setProcessor(qProcr);
		
		assertEquals("Wrong initial status", Status.NONE, beAtA.getStatus());
		assertEquals("Should not be non-zero percent complete", 0d, beAtA.getPercentComplete(), 0);
		
		qProc.execute();
		if (earlyStop) return;
		execLatch.await();
		
		assertEquals("Wrong final status", Status.COMPLETE, beAtA.getStatus());
		assertEquals("Should be 100 percent complete at end", 100d, beAtA.getPercentComplete(), 0);
		
	}

}
