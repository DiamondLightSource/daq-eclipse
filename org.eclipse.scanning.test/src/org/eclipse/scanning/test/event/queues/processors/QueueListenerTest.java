package org.eclipse.scanning.test.event.queues.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.IQueueBroadcaster;
import org.eclipse.scanning.api.event.queues.IQueueProcess;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
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
		qList = new QueueListener<>(processor, childA);
		qList.beanChangePerformed(new BeanEvent<DummyAtom>(childA));
		
		assertEquals("Parent has been updated, even though no changes happened on child", originalParent, parent);
	}
	
	@Test
	public void testIgnoreNonChildren() {
		DummyAtom friend = new DummyAtom("Bilbo", 10);
		
		qList = new QueueListener<>(processor, childA);
		
		friend.setPercentComplete(50d);
		friend.setStatus(Status.RUNNING);
		qList.beanChangePerformed(new BeanEvent<DummyAtom>(friend));
		
		if (getLastBroadcast() != null) {
			assertEquals("Percentage incremented even though non-child in event", getLastBroadcast().getPercentComplete(), originalParent.getPercentComplete(), 0d);
			assertEquals("Status should not have changed as event was non-child", originalParent.getStatus(), getLastBroadcast().getStatus());
		}
	}
	
	@Test
	public void testNormalRunUpdate() {
		qList = new QueueListener<>(processor, queue);
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
		
		//Complete childB percentage & Status
		childB.setPercentComplete(100d);
		childB.setStatus(Status.COMPLETE);
		qList.beanChangePerformed(new BeanEvent<DummyAtom>(childA));
		
		assertTrue("Processor not marked complete on completion of all children", processor.isComplete());
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
