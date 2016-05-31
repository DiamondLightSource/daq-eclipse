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
import org.junit.Before;
import org.junit.Test;

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
	
	@Test
	public void testErrorsOnChangeProcess() throws Exception {
		DummyAtomProcessor dAtProcr = new DummyAtomProcessor();
		qProc = new QueueProcess<Queueable>(dAt, pub, true);
		
		//Mimic processor execution
		dAtProcr.setExecuted();
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
		
		//Try for real
		dAtProcr = new DummyAtomProcessor();
		qProc = new QueueProcess<Queueable>(dAt, pub, true);
		assertFalse("Executed should initially be false", dAtProcr.isExecuted());
		
		//Configure the processor & process
		dAtProcr.setProcessBean(dAt);
		dAtProcr.setQueueProcess(qProc);
		qProc.setProcessor(dAtProcr);
	}
	
	private void runTest(IQueueProcessor<? extends Queueable> qProcr, Queueable beAt) throws Exception {
		qProc = new QueueProcess<Queueable>(beAt, pub, true);
		
		
		//Configure the processor & process
		qProcr.setProcessBean(beAt);
		qProcr.setQueueProcess(qProc);
		qProc.setProcessor(qProcr);
		
		assertEquals("Wrong initial status", Status.NONE, beAt.getStatus());
		assertEquals("Should not be non-zero percent complete", 0d, beAt.getPercentComplete(), 0);
		
		qProc.execute();
		execLatch.await();
		
		assertEquals("Wrong final status", Status.COMPLETE, beAt.getStatus());
		assertEquals("Should be 100 percent complete at end", 100d, beAt.getPercentComplete(), 0);
		
	}

}
