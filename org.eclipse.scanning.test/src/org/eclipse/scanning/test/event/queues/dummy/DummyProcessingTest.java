package org.eclipse.scanning.test.event.queues.dummy;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CountDownLatch;

import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.IQueueProcess;
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
		DummyAtomProcessor dAtProc= new DummyAtomProcessor();
		qProc = new QueueProcess<Queueable>(dAt, pub, true, dAtProc);
		
		assertEquals("Wrong initial status", Status.NONE, dAt.getStatus());
		assertEquals("Should not be non-zero percent complete", 0d, dAt.getPercentComplete(), 0);
		
		qProc.execute();
		execLatch.await();
		
		assertEquals("Wrong final status", Status.COMPLETE, dAt.getStatus());
		assertEquals("Should be 100 percent complete at end", 100d, dAt.getPercentComplete(), 0);
		
	}

}
