package org.eclipse.scanning.test.event.queues.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.event.queues.mocks.DummyQueueable;
import org.eclipse.scanning.test.event.queues.mocks.MockPublisher;
import org.junit.After;

public abstract class AbstractQueueProcessorTest<T extends Queueable>  extends BrokerTest {
	
	protected IConsumerProcess<T> proc;
	
	protected IPublisher<QueueAtom> statPub;
	protected String topic = "active.queue";
	
	protected final CountDownLatch executionLatch = new CountDownLatch(1);

	private Exception thrownException;

	
	@After
	public void tearDown() throws Exception {
		statPub = null;
		proc = null;
	}
	
	protected void createStatusPublisher() throws Exception {
		statPub = new MockPublisher<QueueAtom>(uri, topic);
	}
	
	protected void doExecute() throws Exception {
		Thread th = new Thread(new Runnable() {
			public void run() {
				try {
					proc.execute();
					executionLatch.countDown();
				} catch (Exception e) {
					thrownException = new Exception(e);
				}
			}
		});
		th.setDaemon(true);
		th.setPriority(Thread.MAX_PRIORITY);
		th.start();
		
	}
	
	protected void checkBeanFinalStatus(Status expected) throws Exception {
		checkBeanFinalStatus(expected, false);
	}
	
	protected void checkBeanFinalStatus(Status expected, boolean testLastButTwoPerc) throws Exception {
		if (thrownException != null) { //I think this is a bit ugly, but it's only a test.
			throw thrownException;
		}
		
		DummyQueueable firstBean, lastButTwoBean, lastBean;
		
		List<DummyQueueable> broadcastBeans = ((MockPublisher<QueueAtom>)statPub).getBroadcastBeans();
		firstBean =  broadcastBeans.get(0);
		lastButTwoBean = broadcastBeans.get(broadcastBeans.size()-3);
		lastBean = broadcastBeans.get(broadcastBeans.size()-1);
		
		assertEquals("First bean has wrong status", Status.RUNNING, firstBean.getStatus());
		
		assertEquals("Last bean has wrong status", expected, lastBean.getStatus());
		if (testLastButTwoPerc) {
			System.out.println("Last-but-two % complete: "+lastButTwoBean.getPercentComplete());
			if (expected.equals(Status.TERMINATED)) {
				assertEquals("Second to last bean has wrong status", Status.REQUEST_TERMINATE, lastButTwoBean.getStatus());
			} else if (expected.equals(Status.COMPLETE)){
				assertEquals("Second to last bean has wrong status", Status.RUNNING, lastButTwoBean.getStatus());
				assertEquals("Second to last bean not almost complete", 99d, lastButTwoBean.getPercentComplete(), 5d);
			}	
		}
		if (expected.equals(Status.COMPLETE)) {
			assertEquals("Atom not 100% complete after execution", 100, lastBean.getPercentComplete(), 0);}
		else {
			assertTrue("The percent complete is 100!", lastBean.getPercentComplete()!=100);
		}
	}
	
	protected void checkBeanStatuses(Status[] repStat, Double[] repPerc) throws Exception {
		if (thrownException != null) { //I think this is a bit ugly, but it's only a test.
			throw thrownException;
		}
		
		if(repStat.length != repPerc.length) throw new Exception("Different numbers of statuses & percentages given!");
		
		List<DummyQueueable> broadcastBeans = ((MockPublisher<QueueAtom>)statPub).getBroadcastBeans();
		DummyQueueable bean;
		for(int i = 0; i < repStat.length; i++) {
			bean = broadcastBeans.get(i);
			assertEquals("Unexpected status for bean "+(i+1), repStat[i], bean.getStatus());
			assertEquals("Unexpected percent complete for bean "+(i+1), (double)repPerc[i],bean.getPercentComplete(), 0.05);
		}
	}

	protected void pauseForMockFinalStatus(long timeOut) throws Exception {
		boolean notFinal = true;
		DummyQueueable lastBean = getLastBean(timeOut);
		long startTime = System.currentTimeMillis();
		
		while (notFinal) {
			if (lastBean.getStatus().isFinal()) return;
			Thread.sleep(100);
			
			if (startTime-System.currentTimeMillis() >= timeOut) fail("Final state not found before timeout");
			lastBean = getLastBean(timeOut);
		}
	}
	
	protected void pauseForMockStatus(Status expected, long timeOut) throws Exception {
		DummyQueueable lastBean = getLastBean(timeOut);
		long startTime = System.currentTimeMillis();
		
		while (lastBean.getStatus() != expected) {
			Thread.sleep(100);
			
			if (startTime-System.currentTimeMillis() >= timeOut) fail(expected+" not found before timeout");
			lastBean = getLastBean(timeOut);
		}
	}
	
	private DummyQueueable getLastBean(long timeOut) throws Exception {
		List<DummyQueueable> broadcastBeans = ((MockPublisher<QueueAtom>)statPub).getBroadcastBeans();
		long startTime = System.currentTimeMillis();
		while (broadcastBeans.size() == 0) {
			Thread.sleep(100);
			if (startTime-System.currentTimeMillis() >= timeOut) fail("No beans broadcast before timeout");
			broadcastBeans = ((MockPublisher<QueueAtom>)statPub).getBroadcastBeans();
		}
		
		return broadcastBeans.get(broadcastBeans.size()-1);
	}
}
