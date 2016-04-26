package org.eclipse.scanning.test.event.queues.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.event.queues.beans.IAtomBeanWithQueue;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.junit.Test;

/**
 * Tests operation of a POJO containing an atom queue. Adding, removing, 
 * viewing of queues tested. Methods ensure no duplicates present. 
 * Runtime calculation for the queue also tested.
 * 
 * @author Michael Wharmby
 *
 * @param <T> POJO implementing the IAtomBeanWithQueue interface to be tested.
 * @param <U> Type of {@link QueueAtom} present in queue.
 */
public abstract class AbstractAtomQueueTest<T extends IAtomBeanWithQueue<U>, U extends QueueAtom> extends AbstractBeanTest<T> {
	
	/**
	 * BeanA should have atoms A, B, C; nameA
	 * BeanB should have atoms A, B, C, D, E; nameB
	 */
	
	protected U atomA, atomB, atomC, atomD, atomE;
	protected long timeA = 300, timeB = 1534, timeC = 654, timeD = 20, timeE = 520;
	
	protected String nameA = "Test queue bean A", nameB = "Test queue bean A";
	
	protected void setupQueues() {
		//Populate the beans with the atoms
		beanA.queue().add(atomA);
		beanA.queue().add(atomB);
		beanA.queue().add(atomC);
		
		beanB.queue().add(atomA);
		beanB.queue().add(atomB);
		beanB.queue().add(atomC);
		beanB.queue().add(atomD);
		beanB.queue().add(atomE);
	}
	
	/**
	 * Test basic queue operations.
	 */
	@Test
	public void testAddRemoveAtoms() {
		List<U> atomQueue = beanA.queue().getQueue();
		final int queueSize = atomQueue.size();
		
		assertTrue("atomD addition failed - already present?", beanA.queue().add(atomD));
		assertEquals("Queue size did not change after addition", queueSize+1, beanA.queue().queueSize());
		assertTrue("atomE addition failed - already present?", beanA.queue().add(atomE, 2));
		assertEquals("atomE not at index 2", 2, beanA.queue().getIndex(atomE.getUniqueId()));
		
		assertFalse("atomE addition succeeded - is not in the queue?", beanA.queue().add(atomE)); //Shouldn't be addable, since it's in the queue
		assertTrue("atomE at index 2 removal failed", beanA.queue().remove(2));
		assertTrue("atomA removal by UID string failed", beanA.queue().remove(atomA.getUniqueId()));
		
		List<U> badList = new ArrayList<>();
		badList.add(atomA);
		badList.add(atomD);
		assertFalse("Adding list succeeded, even though duplicates present", beanA.queue().addList(badList)); //Will fail, since D duplicated
		
		List<U> goodList = new ArrayList<>();
		goodList.add(atomA);
		goodList.add(atomE);
		assertTrue("Adding list failed - are atoms already present?", beanA.queue().addList(goodList));
		
		assertTrue(beanA.queue().remove(atomA.getUniqueId()));
		assertTrue(beanA.queue().remove(atomE.getUniqueId()));
		
		assertTrue("Adding list at index failed - are atoms already present?", beanA.queue().addList(goodList, 1));
		
		assertTrue(beanA.queue().remove(atomA.getUniqueId()));
		assertTrue(beanA.queue().remove(atomE.getUniqueId()));
		
		try {
			beanA.queue().add(null);
			fail("Expected NullPointerException not thrown");
		} catch(NullPointerException ex) {
			//expected
		}
		
		try {
			beanA.queue().addList(goodList, 30);
			fail("Expected IndexOutOfBoundsException not thrown");
		} catch(IndexOutOfBoundsException ex) {
			//expected
		}
		
		goodList.add(null);
		try {
			beanA.queue().addList(goodList);
			fail("Expected NullPointerException not thrown");
		} catch(NullPointerException ex) {
			//expected
		}
		
		try {
			beanA.queue().addList(new ArrayList<U>());
			fail("Expected NullPointerException not thrown");
		} catch(NullPointerException ex) {
			//expected
		}
	}
	
	/**
	 * Test viewing of queue and the sequence of stored {@link QueueAtom}s.
	 */
	@Test
	public void testNextLastViewing() {
		List<U> atomQueue = beanB.queue().getQueue();
		final int queueSize = atomQueue.size();
		
		assertEquals("atomA not in the next position", atomA, beanB.queue().viewNext());
		assertEquals("atomE not in the last position", atomE, beanB.queue().viewLast());
		assertEquals("atomA not returned from the next position", atomA, beanB.queue().next());
		assertEquals("Queue has not reduced in size on removal", queueSize-1, beanB.queue().queueSize());
		assertEquals("atomB not in the next position", atomB, beanB.queue().viewNext());
		assertEquals("atomE not returned from the last position", atomE, beanB.queue().last());
		assertEquals("atomD not returned from the last position", atomD, beanB.queue().last());
		assertEquals("Queue has not reduced in size on removal", queueSize-3, beanB.queue().queueSize());
		
		assertEquals("atomA expected at index 0", atomA, beanA.queue().view(0));
	}
	
	/**
	 * Test behaviour of queue in case {@link QueueAtom}s with UIDs (see 
	 * {@link IdBean}) known not to be present are removed/otherwise requested.
	 */
	@Test
	public void testUsingNonExistentUID() {
		try {
			beanA.queue().remove("fish");
			fail("Expected IllegalArgumentException");
		} catch(IllegalArgumentException ex) {
			//Pass
		}
		try {
			beanA.queue().getIndex("fish");
			fail("Expected IllegalArgumentException");
		} catch(IllegalArgumentException ex) {
			//Pass
		}
		try {
			beanA.queue().view("fish");
			fail("Expected IllegalArgumentException");
		} catch(IllegalArgumentException ex) {
			//Pass
		}
	}
	
	/**
	 * Test recovery of {@link QueueAtom} index from a given UID.
	 */
	@Test
	public void testGettingIndexFromUID() {
		assertEquals("atomC expected at index 2 of beanB", 2, beanB.queue().getIndex(atomC.getUniqueId()));
		assertEquals("atomE expected at index 4 of beanB", 4, beanB.queue().getIndex(atomE.getUniqueId()));
		assertEquals("atomB expected at index 1 of beanA", 1, beanA.queue().getIndex(atomB.getUniqueId()));
	}

	/**
	 * Test adding of {@link QueueAtoms} with duplicate UIDs fails.
	 */
	@Test
	public void testAtomDuplication() {
		assertTrue("beanA should contain atomA, but doesn't", beanA.queue().isAtomPresent(atomA));
		assertFalse("beanA shouldn't contain atomD, but does", beanA.queue().isAtomPresent(atomD));
		
		List<U> badList = new ArrayList<>();
		badList.add(atomD);
		badList.add(atomE);
		assertFalse("Atoms in list are not in the queue... but are?", beanA.queue().isAtomInListPresent(badList));
		badList.add(atomD);
		assertTrue("Duplicated atom in list is not detected", beanA.queue().isAtomInListPresent(badList));
		
		badList.clear();
		badList.add(atomA);
		badList.add(atomC);
		assertTrue("Atoms in list are in the queue, but not found?", beanA.queue().isAtomInListPresent(badList));
	}
	
	/**
	 * Test calculation of runtime of queue from the {@link QueueAtom}s present.
	 */
	@Test
	public void testRunTimeCalculation() {
		long beanARunTime = 0, beanBRunTime = 0;
		beanARunTime = beanARunTime + atomA.getRunTime();
		beanARunTime = beanARunTime + atomB.getRunTime();
		beanARunTime = beanARunTime + atomC.getRunTime();
		
		beanBRunTime = beanBRunTime + atomA.getRunTime();
		beanBRunTime = beanBRunTime + atomB.getRunTime();
		beanBRunTime = beanBRunTime + atomC.getRunTime();
		beanBRunTime = beanBRunTime + atomD.getRunTime();
		beanBRunTime = beanBRunTime + atomE.getRunTime();
		
		if (beanARunTime == 0 || beanBRunTime == 0) fail("Runtime is zero, cannot test this.");
		
		//Tests calculation method
		assertEquals("Calculated & expected beanA runTimes differ", beanARunTime, beanA.queue().calculateRunTime());
		assertEquals("Calculated & expected beanB runTimes differ", beanBRunTime, beanB.queue().calculateRunTime());
		
		//Tests setting of runTime on the queue bean
		assertEquals("Reported & expected beanA runtimes differ", beanARunTime, beanA.queue().getRunTime());
		assertEquals("Reported & expected beanB runtimes differ", beanBRunTime, beanB.queue().getRunTime());
		
		//Test updating runtime
		long newRunTime;
		beanA.queue().add(atomD);
		newRunTime = beanARunTime + atomD.getRunTime();
		assertEquals("runTime not updated on adding atom", newRunTime, beanA.runTime());
		beanA.queue().add(atomE, 3);
		newRunTime = newRunTime + atomE.getRunTime();
		assertEquals("runTime not updated on adding atom at index", newRunTime, beanA.runTime());
		beanA.queue().remove(2);
		newRunTime = newRunTime - atomC.getRunTime();
		assertEquals("runTime not updated on removing atom by index", newRunTime, beanA.runTime());
		beanA.queue().remove(atomA.getUniqueId());
		newRunTime = newRunTime - atomA.getRunTime();
		
		List<U> tmpList = new ArrayList<>();
		tmpList.add(atomA);
		tmpList.add(atomC);
		beanA.queue().addList(tmpList);
		newRunTime = newRunTime + atomA.getRunTime() + atomC.getRunTime();
		assertEquals("runTime not updated on adding list", newRunTime, beanA.runTime());
		
		//Next & last should not update the runTime!
		U nextAt = beanA.queue().next();
		assertEquals("runTime changed getting next atom", newRunTime, beanA.runTime());
		U lastAt = beanA.queue().last();
		assertEquals("runTime changed getting last atom", newRunTime, beanA.runTime());
		
		tmpList.clear();
		tmpList.add(nextAt);
		tmpList.add(lastAt);
		beanA.queue().addList(tmpList, 1); 
		//No need to update newRunTime, since only calculating based on what's in the queue
		assertEquals("runTime not updated on adding list at index", newRunTime, beanA.runTime());

	}
	
	/**
	 * Test that a list iterator is returned from the queue and it gives 
	 * expected {@link QueueAtom}s in right sequence.
	 */
	@Test
	public void testListIterator() {
		ListIterator<U> it = beanA.queue().getQueueIterator();
		
		List<QueueAtom> requeue = new ArrayList<>();		
		while (it.hasNext()) {
			requeue.add(it.next());
		}
		assertEquals(3, beanA.queue().queueSize());
		assertEquals(3, requeue.size());
		
		it = beanA.queue().getQueueIterator();
		requeue = new ArrayList<>();
		while (it.hasNext()) {
			requeue.add(it.next());
			it.remove();
		}
		assertEquals(0, beanA.queue().queueSize());
		assertEquals(3, requeue.size());
	}
	

}