package org.eclipse.scanning.test.event.queues.dummy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import org.junit.After;
import org.junit.Before;
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
public class DummyProcessingTest {
	
	private DummyAtom dAt = new DummyAtom("Clotho", 100);
	private DummyBean dBe = new DummyBean("Lachesis", 200);
	private DummyHasQueue dHQ = new DummyHasQueue("Atropos", 300);
	
	private DummyAtomProcessor dAtProcr;
	private DummyBeanProcessor dBeProcr;
	private DummyHasQueueProcessor dHQProcr;
	
	private CountDownLatch execLatch;
	
	//Surrounding infrastructure config
	private IQueueProcess<Queueable> qProc;
	private IPublisher<Queueable> pub = new MockPublisher<Queueable>(null, null);
	
	@Before
	public void setUp() {
		execLatch = new CountDownLatch(1);
	}
	
	@After
	public void tearDown() {
		dAtProcr = null;
		dBeProcr = null;
		dHQProcr = null;
		execLatch = null;
	}
	
	@Test
	public void testDummyAtomRunning() throws Exception {
		dAt.setLatch(execLatch);
		dAtProcr= new DummyAtomProcessor();
		
		runTest(dAtProcr, dAt);
	}
	
	@Test
	public void testDummyBeanRunning() throws Exception {
		dBe.setLatch(execLatch);
		dBeProcr = new DummyBeanProcessor();
		
		runTest(dBeProcr, dBe);
	}
	
	@Test
	public void testDummyHasQueueRunning() throws Exception {
		dHQ.setLatch(execLatch);
		dHQProcr = new DummyHasQueueProcessor();
		
		runTest(dHQProcr, dHQ);
	}
	
	/**
	 * Tests that once setExecuted or execute called, configuration of 
	 * processor cannot be changed.
	 * @throws Exception
	 */
	@Test
	public void testChangingProcessorAfterExecution() throws Exception {
		DummyAtom dAtA = new DummyAtom("Zeus", 400);
		dAtA.setLatch(execLatch);
		dAtProcr = new DummyAtomProcessor();
		qProc = new QueueProcess<Queueable>(dAtA, pub, true);
		
		//Mimic processor execution
		dAtProcr.setExecuted();
		try {
			dAtProcr.setProcessBean(dAtA);
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
		
		//Try for real
		dAtProcr = new DummyAtomProcessor();
		qProc = new QueueProcess<Queueable>(dAtA, pub, true);
		assertFalse("Executed should initially be false", dAtProcr.isExecuted());
		
		//Configure the processor & process
		dAtProcr.setProcessBean(dAtA);
		dAtProcr.setQueueProcess(qProc);
		qProc.setProcessor(dAtProcr);
		
		qProc.execute();
		try {
			dAtProcr.setProcessBean(dAtA);
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
		execLatch.await();
		assertEquals("Wrong final status", Status.COMPLETE, dAtA.getStatus());
	}
	
	/**
	 * Test that type checking of the setProcessBean method prevents the wrong 
	 * bean being accepted by the processor.
	 * @throws Exception
	 */
	@Test
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
	@Test
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
