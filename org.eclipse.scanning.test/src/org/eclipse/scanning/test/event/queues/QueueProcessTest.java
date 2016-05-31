package org.eclipse.scanning.test.event.queues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.QueueProcess;
import org.eclipse.scanning.test.event.queues.mocks.DummyBean;
import org.eclipse.scanning.test.event.queues.mocks.DummyHasQueue;
import org.eclipse.scanning.test.event.queues.mocks.MockPublisher;
import org.eclipse.scanning.test.event.queues.mocks.MockQueueProcessor;
import org.junit.Before;
import org.junit.Test;

/**
 * Testing calls of QueueProcess down to a Mock IQueueProcess,
 * to check execution, termination & pausing happen as expected.
 * 
 * SuppressWarnings annotations are valid, since:
 *  a) this is a test.
 *  b) we know the generic types in use.
 * 
 * @author Michael Wharmby
 *
 */
public class QueueProcessTest {

	private QueueProcess<DummyBean> qProc;
	private MockQueueProcessor<DummyBean> mockProc;
	private CountDownLatch execLatch;
	
	//Settings for QueueProcess
	private DummyBean dummy = new DummyBean();
	private IPublisher<DummyBean> pub = new MockPublisher<DummyBean>(null, null);
	private boolean blocking = true;
	
	@Before
	public void setUp() {
		execLatch = new CountDownLatch(1);
		mockProc = new MockQueueProcessor<DummyBean>(execLatch);
		
		qProc = new QueueProcess<DummyBean>(dummy, pub, blocking, mockProc);
	}
	
	
	/**
	 * Sets a countdown (which goes in steps of 10ms whilst pausing for 10ms - 
	 * @see org.eclipse.scanning.test.event.queues.mocks.MockQueueProcessor )
	 * Run is complete when counter reaches 0ms.
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testExecute() throws Exception {
		mockProc.setCounter(150);
		executeThread();
		
		execLatch.await(5, TimeUnit.SECONDS);
		MockQueueProcessor<DummyBean> processor= (MockQueueProcessor<DummyBean>)qProc.getProcessor();
		assertTrue("Mock processor didn't get the execute signal", processor.isExecuted());
		assertTrue("Mock processor didn't execute to completion", processor.isComplete());
		assertTrue("Mock processor ran for a long time!", processor.getRunTime() < 500l);
	}
	
	/**
	 * Sets a countdown (which goes in steps of 10ms whilst pausing for 10ms - 
	 * @see org.eclipse.scanning.test.event.queues.mocks.MockQueueProcessor )
	 * Terminate interrupts before counter = 0ms.
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testTerminate() throws Exception {
		mockProc.setCounter(1500);
		executeThread();
		Thread.sleep(200);
		
		qProc.terminate();
		
		execLatch.await(5, TimeUnit.SECONDS);
		MockQueueProcessor<DummyBean> processor= (MockQueueProcessor<DummyBean>)qProc.getProcessor();
		assertTrue("Mock processor didn't get the execute signal", processor.isExecuted());
		assertTrue("Mock processor never got a terminated signal", processor.isTerminated());
		assertTrue("Mock processor didn't run for long", processor.getRunTime() > 50l);
		assertFalse("Mock processor shouldn't be complete", processor.isComplete());
	}
	
	/**
	 * Sets a countdown (which goes in steps of 10ms whilst pausing for 10ms - 
	 * @see org.eclipse.scanning.test.event.queues.mocks.MockQueueProcessor )
	 * Run is complete when counter reaches 0ms. Pause causes process to take 
	 * longer, which is observed in the runTime reported by the processor.
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testPause() throws Exception {
		mockProc.setCounter(300);
		executeThread();
		Thread.sleep(500);
		qProc.pause();
		Thread.sleep(400);
		qProc.resume();
		
		execLatch.await(5, TimeUnit.SECONDS);
		MockQueueProcessor<DummyBean> processor= (MockQueueProcessor<DummyBean>)qProc.getProcessor();
		assertTrue("Mock processor didn't get the execute signal", processor.isExecuted());
		assertTrue("Mock processor didn't execute to completion", processor.isComplete());
		assertTrue("Mock processor didn't pause correctly", processor.getRunTime() < 650l);
	}
	
	@Test
	public void testBroadcasting() throws EventException {
		dummy = new DummyBean("Vladimir", 750);
		
		//Test general broadcast
		String message = "Hello world";
		qProc.broadcast(null, null, message);
		
		//Test setting of status, percent complete & both
		qProc.broadcast(Status.QUEUED);
		qProc.broadcast(50);
		
		//Test setting both status & percent complete
		qProc.broadcast(Status.COMPLETE, 100d, null);
		
		List<DummyHasQueue> broadcasted = ((MockPublisher<DummyBean>) pub).getBroadcastBeans();
		String[] messages = new String[]{"Hello world", "Hello world", "Hello world", "Hello world"};
		Status[] statuses = new Status[]{Status.NONE, Status.QUEUED, Status.QUEUED, Status.COMPLETE};
		double[] percents = new double[]{0d, 0d, 50d, 100d};
		
		for (int i = 0; i < broadcasted.size(); i++) {
			DummyHasQueue bean = broadcasted.get(i);
			assertEquals("Bean nr. "+i+" has wrong message", messages[i], bean.getMessage());
			assertEquals("Bean nr. "+i+" has wrong status", statuses[i], bean.getStatus());
			assertEquals("Bean nr. "+i+" has wrong percent complete", percents[i], bean.getPercentComplete(), 0);
		}
	}
	
	private void executeThread() {
		//Run the execute() call in a separate thread, so we can terminate 
		//etc. it
				Thread th = new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							qProc.execute();
						} catch (Exception e) {
							System.out.println("***********************");
							System.out.println("Error thrown during queue processor execution");
							System.out.println("***********************");
							e.printStackTrace();
						}
					}
				});
				th.setDaemon(true);
				th.setPriority(Thread.MAX_PRIORITY);
				th.start();
	}

}
