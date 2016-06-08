package org.eclipse.scanning.test.event.queues.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.processors.QueueListener;
import org.eclipse.scanning.test.event.queues.dummy.DummyAtom;
import org.eclipse.scanning.test.event.queues.dummy.DummyHasQueue;
import org.eclipse.scanning.test.event.queues.mocks.MockQueueProcessor;
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
	
	private MockQueueProcessor<DummyHasQueue> processor;
	
	@Before
	public void setUp() {
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
		
		processor = new MockQueueProcessor<DummyHasQueue>(parent, new CountDownLatch(1));
	}
	
	@Test
	public void testNothingHappened() {
		qList = new QueueListener<>(processor, parent, childA);
		qList.beanChangePerformed(new BeanEvent<DummyAtom>(childA));
		
		assertEquals("Parent has been updated, even thogh no changes happened on child", originalParent, parent);
	}
	
	@Test
	public void testIgnoreNonChildren() {
		DummyAtom friend = new DummyAtom("Bilbo", 10);
		
		friend.setPercentComplete(50d);
		friend.setStatus(Status.RUNNING);
		qList.beanChangePerformed(new BeanEvent<DummyAtom>(friend));
		
		assertEquals("Percentage incremented even though non-child in event", parent.getPercentComplete(), originalParent.getPercentComplete(), 0d);
		assertEquals("Status should not have changed as event was non-child", originalParent.getStatus(), parent.getStatus());
		
	}
	
	@Test
	public void testNormalRunUpdate() {
		qList = new QueueListener<>(processor, parent, queue);
		//Change childA percentage only (not active)
		childA.setPercentComplete(50d);
		qList.beanChangePerformed(new BeanEvent<DummyAtom>(childA));
		
		assertEquals("Percentage incremented even though child not active", parent.getPercentComplete(), originalParent.getPercentComplete(), 0d);
		assertEquals("Status changed even though child not active", originalParent.getStatus(), parent.getStatus());
		
		//Now with the status actually set...
		childA.setStatus(Status.RUNNING);
		qList.beanChangePerformed(new BeanEvent<DummyAtom>(childA));
		
		assertEquals("Percentage incorrectly incremented", 28.75d, parent.getPercentComplete(), 0d);
		assertEquals("Status should not have changed", originalParent.getStatus(), parent.getStatus());
		
		//Complete childA percentage & Status
		childA.setPercentComplete(100d);
		childA.setStatus(Status.COMPLETE);
		qList.beanChangePerformed(new BeanEvent<DummyAtom>(childA));
		
		assertEquals("Percentage incorrectly incremented", 52.5d, parent.getPercentComplete(), 0d);
		assertEquals("Status should not have changed", originalParent.getStatus(), parent.getStatus());
		
		//Complete childB percentage & Status
		childB.setPercentComplete(100d);
		childB.setStatus(Status.COMPLETE);
		qList.beanChangePerformed(new BeanEvent<DummyAtom>(childA));
		
		assertTrue("Processor not marked complete on completion of all children", processor.isComplete());
	}

}
