package org.eclipse.scanning.test.event.queues.processors;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.IQueueBroadcaster;
import org.eclipse.scanning.api.event.queues.IQueueProcess;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.beans.IAtomWithChildQueue;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.QueueProcess;
import org.eclipse.scanning.event.queues.processors.QueueListener;
import org.eclipse.scanning.test.event.queues.dummy.DummyAtom;
import org.eclipse.scanning.test.event.queues.dummy.DummyHasQueue;
import org.eclipse.scanning.test.event.queues.mocks.MockPublisher;
import org.eclipse.scanning.test.event.queues.mocks.MockQueueProcessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * QueueListener listens to the progress (status/percent complete) of one or 
 * more beans on a child queue & updates the progress of the parent bean.
 * 
 * @author Michael Wharmby
 *
 */
public class QueueListenerTest {
	
	private QueueListener<DummyHasQueue, DummyAtom> qList;
	private DummyHasQueue parent, originalParent;
	private double initPercent = 5d;
	private DummyAtom childA, childB;
	private List<DummyAtom> queue;
	
	private IPublisher<DummyHasQueue> statPub;
	private IQueueProcessor<DummyHasQueue> processor;
	private IQueueBroadcaster<DummyHasQueue> broadcaster;
	private CountDownLatch latch;
	
	@Before
	public void setUp() throws Exception {
		//Set initial state on parent
		parent = new DummyHasQueue("Big momma", 60);
		parent.setStatus(Status.RUNNING);
		parent.setPercentComplete(initPercent);
		originalParent = new DummyHasQueue();
		originalParent.merge(parent);
		
		//Set initial state on child
		childA = new DummyAtom("Baby", 10);
		childA.setStatus(Status.SUBMITTED);
		childA.setPercentComplete(0d);
		childB = new DummyAtom("Bobby", 10);
		childB.setStatus(Status.SUBMITTED);
		childB.setPercentComplete(0d);
		
		queue = new ArrayList<>();
		queue.add(childA);
		queue.add(childB);
		
		statPub = new MockPublisher<>(null, "test.topic");
		broadcaster = new QueueProcess<>(parent, statPub, false);
		processor = new MockQueueProcessor<>(broadcaster, parent, new CountDownLatch(1));//TODO Update to allow broadcaster to be set
		((IQueueProcess<?>)broadcaster).setProcessor(processor);
		
		latch = new CountDownLatch(1);
	}
	
	@After
	public void tearDown() {
		parent = null;
		originalParent = null;
		childA = null;
		childB = null;
		queue = null;

		statPub = null;
		broadcaster = null;
		processor = null;
	}
	
	@Test
	public void testNothingHappened() {
		//This is set before Listener creation so the bean status doesn't appear to change
		childA.setStatus(Status.RUNNING);
		childA.setPercentComplete(50d);
		qList = new QueueListener<>(processor, latch, childA);
		qList.beanChangePerformed(new BeanEvent<DummyAtom>(childA));
		
		assertEquals("Parent has been broadcast, even though no changes happened on child", null, getLastBroadcast());
	}
	
	@Test
	public void testIgnoreNonChildren() {
		DummyAtom friend = new DummyAtom("Bilbo", 10);
		
		qList = new QueueListener<>(processor, latch, childA);
		
		friend.setPercentComplete(50d);
		friend.setStatus(Status.RUNNING);
		qList.beanChangePerformed(new BeanEvent<DummyAtom>(friend));
		
		if (getLastBroadcast() != null) {
			assertEquals("Percentage incremented even though non-child in event", getLastBroadcast().getPercentComplete(), originalParent.getPercentComplete(), 0d);
			assertEquals("Status should not have changed as event was non-child", originalParent.getStatus(), getLastBroadcast().getStatus());
		}
	}
	
	@Test
	public void testNormalRun() {
		qList = new QueueListener<>(processor, latch, queue);
		//Change childA percentage only (not active)
		childA.setPercentComplete(50d);
		qList.beanChangePerformed(new BeanEvent<DummyAtom>(childA));
		
		assertEquals("Percentage incorrectly incremented", 28.75d, getLastBroadcast().getPercentComplete(), 0d);
		assertEquals("Status changed even though child not active", originalParent.getStatus(), getLastBroadcast().getStatus());
		
		//Now with the status actually set...
		childA.setStatus(Status.RUNNING);
		qList.beanChangePerformed(new BeanEvent<DummyAtom>(childA));
		
		assertEquals("Percentage incorrectly incremented", 28.75d, getLastBroadcast().getPercentComplete(), 0d);
		assertEquals("Status should not have changed", originalParent.getStatus(), getLastBroadcast().getStatus());
		
		//Complete childA percentage & Status
		childA.setPercentComplete(100d);
		childA.setStatus(Status.COMPLETE);
		qList.beanChangePerformed(new BeanEvent<DummyAtom>(childA));
		
		assertEquals("Percentage incorrectly incremented", 52.5d, getLastBroadcast().getPercentComplete(), 0d);
		assertEquals("Status should not have changed", originalParent.getStatus(), getLastBroadcast().getStatus());
		assertEquals("Processor latch released before all children complete", 1, latch.getCount(), 0);
		
		//Complete childB percentage & Status
		childB.setStatus(Status.RUNNING);
		qList.beanChangePerformed(new BeanEvent<DummyAtom>(childB));
		childB.setPercentComplete(100d);
		childB.setStatus(Status.COMPLETE);
		qList.beanChangePerformed(new BeanEvent<DummyAtom>(childB));
		
		//All beans now complete, so latch should have been released
		assertEquals("Processor latch not released on completion of all children", 0, latch.getCount(), 0);
	}
	
	/**
	 * Tests REQUEST_TERMINATE and TERMINATED both lead to parent entering 
	 * REQUEST_TERMINATE state
	 */
	@Test
	public void testRunExternalTermination() {
		Status[] termCalls = new Status[]{Status.REQUEST_TERMINATE, Status.TERMINATED};
		for (int i = 0; i < 2; i++) {
			childA.setStatus(Status.RUNNING);
			qList = new QueueListener<>(processor, latch, queue);
			childA.setPercentComplete(50d);
			qList.beanChangePerformed(new BeanEvent<DummyAtom>(childA));

			assertEquals("Percentage incorrectly incremented", 28.75d, getLastBroadcast().getPercentComplete(), 0d);

			childA.setStatus(termCalls[i]);
			qList.beanChangePerformed(new BeanEvent<DummyAtom>(childA));

			assertEquals("Percentage incorrectly incremented", 28.75d, getLastBroadcast().getPercentComplete(), 0d);
			assertEquals("Parent status not changed", Status.REQUEST_TERMINATE, getLastBroadcast().getStatus());
		}
	}
	
	/**
	 * Tests that both pausing & resuming from the child lead to the parent 
	 * moving to a REQUEST_PAUSE & REQUEST_RESUME. Also ensures {ACTION} and 
	 * REQUEST_{ACTION} Statuses are handled the same.
	 */
	@Test
	public void testRunExternalPause() {
		childA.setStatus(Status.RUNNING);
		qList = new QueueListener<>(processor, latch, queue);
		childA.setPercentComplete(50d);
		qList.beanChangePerformed(new BeanEvent<DummyAtom>(childA));

		assertEquals("Percentage incorrectly incremented", 28.75d, getLastBroadcast().getPercentComplete(), 0d);

		childA.setStatus(Status.REQUEST_PAUSE);
		qList.beanChangePerformed(new BeanEvent<DummyAtom>(childA));

		assertEquals("Percentage incorrectly incremented", 28.75d, getLastBroadcast().getPercentComplete(), 0d);
		assertEquals("Parent status not changed", Status.REQUEST_PAUSE, getLastBroadcast().getStatus());
		assertEquals("Parent has wrong queue message", "Pause requested from '"+childA.getName()+"'", ((IAtomWithChildQueue)getLastBroadcast()).getQueueMessage());
		
		//Mimic effect of processor
		parent.setStatus(Status.PAUSED);
		
		childA.setStatus(Status.REQUEST_RESUME);
		qList.beanChangePerformed(new BeanEvent<DummyAtom>(childA));
		
		assertEquals("Percentage incorrectly incremented", 28.75d, getLastBroadcast().getPercentComplete(), 0d);
		assertEquals("Parent status not changed", Status.REQUEST_RESUME, getLastBroadcast().getStatus());
		assertEquals("Parent has wrong queue message", "Resume requested from '"+childA.getName()+"'", ((IAtomWithChildQueue)getLastBroadcast()).getQueueMessage());
		
		//Mimic effect of processor
		parent.setStatus(Status.RESUMED);
		childA.setPercentComplete(80d);
		qList.beanChangePerformed(new BeanEvent<DummyAtom>(childA));
		assertEquals("Percentage incorrectly incremented", 43d, getLastBroadcast().getPercentComplete(), 0d);
		
		//Mimic effect of processor
		parent.setStatus(Status.RUNNING);
		
		childA.setStatus(Status.PAUSED);
		qList.beanChangePerformed(new BeanEvent<DummyAtom>(childA));

		assertEquals("Percentage incorrectly incremented", 43d, getLastBroadcast().getPercentComplete(), 0d);
		assertEquals("Parent status not changed", Status.REQUEST_PAUSE, getLastBroadcast().getStatus());
		assertEquals("Parent has wrong queue message", "Pause requested from '"+childA.getName()+"'", ((IAtomWithChildQueue)getLastBroadcast()).getQueueMessage());
		
		//Mimic effect of processor
		parent.setStatus(Status.PAUSED);
		childA.setStatus(Status.RESUMED);
		qList.beanChangePerformed(new BeanEvent<DummyAtom>(childA));
		
		assertEquals("Percentage incorrectly incremented", 43d, getLastBroadcast().getPercentComplete(), 0d);
		assertEquals("Parent status not changed", Status.REQUEST_RESUME, getLastBroadcast().getStatus());
		assertEquals("Parent has wrong queue message", "Resume requested from '"+childA.getName()+"'", ((IAtomWithChildQueue)getLastBroadcast()).getQueueMessage());
	}

	private Queueable getLastBroadcast() {
		List<Queueable> broadBeans = ((MockPublisher<?>)statPub).getBroadcastBeans();
		if (broadBeans.size() == 0) {
			return null;
		} else {
			return broadBeans.get(broadBeans.size()-1);
		}
	}
}
