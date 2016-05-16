package org.eclipse.scanning.test.event.queues;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.event.queues.QueueProcess;
import org.eclipse.scanning.test.event.queues.mocks.DummyBean;
import org.eclipse.scanning.test.event.queues.mocks.MockPublisher;
import org.eclipse.scanning.test.event.queues.mocks.MockQueueProcessor;
import org.junit.Before;
import org.junit.Test;

public class QueueProcessTest {
	
	
	
	private QueueProcess<DummyBean> qProc;
	private MockQueueProcessor mockProc;
	private CountDownLatch execLatch;
	
	//Settings for QueueProcess
	private DummyBean dummy = new DummyBean();
	private IPublisher<DummyBean> pub = new MockPublisher<DummyBean>(null, null);
	private boolean blocking = true;
	
	@Before
	public void setUp() {
		execLatch = new CountDownLatch(1);
		mockProc = new MockQueueProcessor(execLatch);
		
		qProc = new QueueProcess<DummyBean>(dummy, pub, blocking, mockProc);
	}
	
	
	@Test
	public void testExecute() throws Exception {
		mockProc.setCounter(150);
		executeThread();
		
		execLatch.await(5, TimeUnit.SECONDS);
		MockQueueProcessor processor= (MockQueueProcessor)qProc.getProcessor();
		assertTrue("Mock processor didn't get the execute signal", processor.isExecuted());
		assertTrue("Mock processor didn't execute to completion", processor.isComplete());
		assertTrue("Mock processor ran for a long time!", processor.getRunTime() < 500l);
	}
	
	@Test
	public void testTerminate() throws Exception {
		mockProc.setCounter(1500);
		executeThread();
		Thread.sleep(200);
		
		qProc.terminate();
		
		execLatch.await(5, TimeUnit.SECONDS);
		MockQueueProcessor processor= (MockQueueProcessor)qProc.getProcessor();
		assertTrue("Mock processor didn't get the execute signal", processor.isExecuted());
		assertTrue("Mock processor never got a terminated signal", processor.isTerminated());
		assertTrue("Mock processor didn't run for long", processor.getRunTime() > 50l);
		assertFalse("Mock processor shouldn't be complete", processor.isComplete());
	}
	
	@Test
	public void testPause() throws Exception {
		mockProc.setCounter(300);
		executeThread();
		Thread.sleep(500);
		qProc.pause();
		Thread.sleep(400);
		qProc.resume();
		
		execLatch.await(5, TimeUnit.SECONDS);
		MockQueueProcessor processor= (MockQueueProcessor)qProc.getProcessor();
		assertTrue("Mock processor didn't get the execute signal", processor.isExecuted());
		assertTrue("Mock processor didn't execute to completion", processor.isComplete());
		assertTrue("Mock processor didn't pause correctly", processor.getRunTime() < 650l);
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
							System.out.println("Error thrown during queue processor exection");
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
