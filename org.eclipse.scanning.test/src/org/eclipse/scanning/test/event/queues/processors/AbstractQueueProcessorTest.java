package org.eclipse.scanning.test.event.queues.processors;

import java.util.concurrent.CountDownLatch;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.IQueueProcess;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.QueueProcess;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.event.queues.dummy.DummyHasQueue;
import org.eclipse.scanning.test.event.queues.mocks.MockPublisher;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractQueueProcessorTest extends BrokerTest { //<T extends Queueable> 
	
	protected IQueueProcess<Queueable> qProc;
	
	protected IPublisher<Queueable> statPub;
	protected String topic = "active.queue";
	
	protected CountDownLatch execLatch = new CountDownLatch(1);

	private Exception thrownException;

	@Before
	public void setUp() {
		execLatch = new CountDownLatch(1);
		
		statPub = new MockPublisher<Queueable>(uri, topic);
		
		localSetup();
	}
	
	protected abstract void localSetup();
	
	
	@After
	public void tearDown() throws Exception {
		localTearDown();
		if (qProc != null) qProc.terminate();
		statPub = null;
		qProc = null;
		
		execLatch = null;
	}
	
	protected abstract void localTearDown();
	
	protected void doExecute(IQueueProcessor<? extends Queueable> qProcr, Queueable bean) throws Exception {
		doExecute(qProcr, bean, bean);
	}
	
	/**
	 * Generic method for running a queue processor. When complete, it releases the execLatch.
	 * execLatch.await() should be placed directly after this call.
	 * @param qProcr
	 * @param procBean
	 * @param procrBean
	 * @throws Exception
	 */
	protected void doExecute(IQueueProcessor<? extends Queueable> qProcr, Queueable procBean, Queueable procrBean) throws Exception {
		qProc = new QueueProcess<Queueable>(procBean, statPub, true);
		
		//Configure the QueueProcess & QueueProcessor
		qProcr.setQueueProcess(qProc);
		qProcr.setProcessBean(procrBean);
		qProc.setProcessor(qProcr);
		
		Thread th = new Thread(new Runnable() {
			public void run() {
				try {
					qProc.execute();
					execLatch.countDown();
				} catch (Exception e) {
					thrownException = new Exception(e);
				}
			}
		});
		th.setDaemon(true);
		th.setPriority(Thread.MAX_PRIORITY);
		th.start();
	}
	
	/**
	 * This is designed to throw an exception (testing the locking of config change behaviour).
	 * Should be surrounded by a try/catch block, with a fail in the try immediately after the call.
	 * @param qProcr
	 * @param procrBean
	 * @throws Exception
	 */
	protected void changeBeanAfterExecution(IQueueProcessor<? extends Queueable> qProcr, Queueable procrBean) throws EventException {
		qProcr.setExecuted();
		qProcr.setProcessBean(procrBean);
	}

	/**
	 * This is designed to throw an exception (testing the locking of config change behaviour).
	 * Should be surrounded by a try/catch block, with a fail in the try immediately after the call.
	 * @param qProcr
	 * @throws EventException
	 */
	protected void changeProcessAfterExecution(IQueueProcessor<? extends Queueable> qProcr) throws EventException {
		qProcr.setExecuted();
		qProcr.setQueueProcess(qProc);
	}
	
	protected void waitForBeanStatus(Queueable bean, Status state, Long timeout) throws Exception {
		DummyHasQueue lastBean;
		long startTime = System.currentTimeMillis();
		long runTime;
		
		while (true) {
			lastBean = ((MockPublisher<Queueable>)statPub).getLastBean();
			if ((lastBean != null) && (lastBean.getStatus().equals(state))) {
				break;
			}
			Thread.sleep(10);
			runTime = System.currentTimeMillis() - startTime;
			if ((timeout != null) && (runTime >= timeout)) {
				throw new Exception("Bean state not reached before timeout");
			}
		}
	}


//	
//	protected void checkBeanFinalStatus(Status expected) throws Exception {
//		checkBeanFinalStatus(expected, false);
//	}
//	
//	protected void checkBeanFinalStatus(Status expected, boolean testLastButTwoPerc) throws Exception {
//		if (thrownException != null) { //I think this is a bit ugly, but it's only a test.
//			throw thrownException;
//		}
//		
//		DummyHasQueue firstBean, lastButTwoBean, lastBean;
//		
//		List<DummyHasQueue> broadcastBeans = ((MockPublisher<QueueAtom>)statPub).getBroadcastBeans();
//		firstBean =  broadcastBeans.get(0);
//		lastButTwoBean = broadcastBeans.get(broadcastBeans.size()-3);
//		lastBean = broadcastBeans.get(broadcastBeans.size()-1);
//		
//		assertEquals("First bean has wrong status", Status.RUNNING, firstBean.getStatus());
//		
//		assertEquals("Last bean has wrong status", expected, lastBean.getStatus());
//		if (testLastButTwoPerc) {
//			System.out.println("Last-but-two % complete: "+lastButTwoBean.getPercentComplete());
//			if (expected.equals(Status.TERMINATED)) {
//				assertEquals("Second to last bean has wrong status", Status.REQUEST_TERMINATE, lastButTwoBean.getStatus());
//			} else if (expected.equals(Status.COMPLETE)){
//				assertEquals("Second to last bean has wrong status", Status.RUNNING, lastButTwoBean.getStatus());
//				assertEquals("Second to last bean not almost complete", 99d, lastButTwoBean.getPercentComplete(), 5d);
//			}	
//		}
//		if (expected.equals(Status.COMPLETE)) {
//			assertEquals("Atom not 100% complete after execution", 100, lastBean.getPercentComplete(), 0);}
//		else {
//			assertThat("The percent complete is 100!", lastBean.getPercentComplete(), is(not(100)));
//		}
//	}
//	
//	protected void checkBeanStatuses(Status[] repStat, Double[] repPerc) throws Exception {
//		if (thrownException != null) { //I think this is a bit ugly, but it's only a test.
//			throw thrownException;
//		}
//		
//		if(repStat.length != repPerc.length) throw new Exception("Different numbers of statuses & percentages given!");
//		
//		List<DummyHasQueue> broadcastBeans = ((MockPublisher<QueueAtom>)statPub).getBroadcastBeans();
//		DummyHasQueue bean;
//		for(int i = 0; i < repStat.length; i++) {
//			bean = broadcastBeans.get(i);
//			assertEquals("Unexpected status for bean "+(i+1), repStat[i], bean.getStatus());
//			assertEquals("Unexpected percent complete for bean "+(i+1), (double)repPerc[i],bean.getPercentComplete(), 0.05);
//		}
//	}
//
//	protected void pauseForMockFinalStatus(long timeOut) throws Exception {
//		boolean notFinal = true;
//		DummyHasQueue lastBean = getLastBean(timeOut);
//		long startTime = System.currentTimeMillis();
//		
//		while (notFinal) {
//			if (lastBean.getStatus().isFinal()) return;
//			Thread.sleep(100);
//			
//			if (startTime-System.currentTimeMillis() >= timeOut) fail("Final state not found before timeout");
//			lastBean = getLastBean(timeOut);
//		}
//	}
//	
//	protected void pauseForMockStatus(Status expected, long timeOut) throws Exception {
//		DummyHasQueue lastBean = getLastBean(timeOut);
//		long startTime = System.currentTimeMillis();
//		
//		while (lastBean.getStatus() != expected) {
//			Thread.sleep(100);
//			
//			if (startTime-System.currentTimeMillis() >= timeOut) fail(expected+" not found before timeout");
//			lastBean = getLastBean(timeOut);
//		}
//	}
//	
//	private DummyHasQueue getLastBean(long timeOut) throws Exception {
//		List<DummyHasQueue> broadcastBeans = ((MockPublisher<QueueAtom>)statPub).getBroadcastBeans();
//		long startTime = System.currentTimeMillis();
//		while (broadcastBeans.size() == 0) {
//			Thread.sleep(100);
//			if (startTime-System.currentTimeMillis() >= timeOut) fail("No beans broadcast before timeout");
//			broadcastBeans = ((MockPublisher<QueueAtom>)statPub).getBroadcastBeans();
//		}
//		
//		return broadcastBeans.get(broadcastBeans.size()-1);
//	}
}
