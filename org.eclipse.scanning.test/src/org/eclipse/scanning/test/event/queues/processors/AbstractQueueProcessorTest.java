package org.eclipse.scanning.test.event.queues.processors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.util.List;

import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.processors.AbstractQueueProcessor;
import org.eclipse.scanning.test.event.queues.mocks.DummyQueueable;
import org.eclipse.scanning.test.event.queues.mocks.MockPublisher;
import org.junit.After;

public abstract class AbstractQueueProcessorTest<T extends Queueable> {
	
	protected AbstractQueueProcessor<T> proc;
	
	protected IPublisher<QueueAtom> statPub;
	protected URI uri;
	protected String topic = "active.queue";

	private Exception thrownException;
	
	@After
	public void tearDown() throws Exception {
		statPub = null;
		proc = null;
	}
	
	protected void createStatusPublisher() throws Exception {
		uri = new URI("vm://localhost?broker.persistent=false");
		statPub = new MockPublisher<QueueAtom>(uri, topic);
	}
	
	protected void doExecute() throws Exception {
		Thread th = new Thread(new Runnable() {
			public void run() {
				try {
					proc.execute();
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
			assertThat("The percent complete is 100!", lastBean.getPercentComplete(), is(not(100)));
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

}
