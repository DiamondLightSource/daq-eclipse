package org.eclipse.scanning.test.event.queues;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.QueueProcess;
import org.eclipse.scanning.test.event.queues.mocks.DummyBean;
import org.eclipse.scanning.test.event.queues.mocks.MockQueueProcessor;
import org.junit.Before;
import org.junit.Test;

public class QueueProcessTest {
	
	private QueueProcess<DummyBean> qProc;
	private MockQueueProcessor mockProc;
	private CountDownLatch execLatch;
	
	//Settings for QueueProcess
	private DummyBean dummy = new DummyBean();
	private IPublisher<DummyBean> pub = null;
	private boolean blocking = true;
	
	@Before
	public void setUp() {
		
		mockProc = new MockQueueProcessor(execLatch);
		
		qProc = new QueueProcess(dummy, pub, blocking, mockProc);
	}
	
	
	@Test
	public void testExecute() throws Exception {
		mockProc.setDelay(250);
		executeThread();
		
		execLatch.await(5, TimeUnit.SECONDS);
		MockQueueProcessor processor= (MockQueueProcessor)qProc.getProcessor();
		assertTrue("Mock processor didn't get the execute signal", processor.isExecuted());
		assertTrue("Mock processor didn't execute to completion", processor.isComplete());
	}
	
	@Test
	public void testTerminate() throws Exception {
		mockProc.setDelay(1500);
		executeThread();
		Thread.sleep(500);
		
		qProc.terminate();
		
		execLatch.await(5, TimeUnit.SECONDS);
		MockQueueProcessor processor= (MockQueueProcessor)qProc.getProcessor();
		assertTrue("Mock processor didn't get the execute signal", processor.isExecuted());
		assertTrue("Mock processor never got a terminated signal", processor.isTerminated());
		assertTrue("Mock processor didn't run for long", processor.getRunTime() > 50l);
		assertFalse("Mock processor shouldn't be complete", processor.isComplete());
	}
	
//	@Test
//	public void testPause() {
//		mockProc.setDelay(1500);
//		executeThread();
//		Thread.sleep(500);
//		qProc
//	}
	
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
