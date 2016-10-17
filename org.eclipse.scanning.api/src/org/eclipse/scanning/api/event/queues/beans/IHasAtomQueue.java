package org.eclipse.scanning.api.event.queues.beans;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Interface to enforce the composition rather than inheritance relationship 
 * of IAtomQueues within beans.
 * 
 * TODO Make child of {@link IHasChildQueue}
 * FIXME Update java-doc
 * 
 * @author Michael Wharmby
 *
 * @param <T> Class extending QueueAtom; this class will be held in the Queue
 *            inside this Atom/Bean
 */
public interface IHasAtomQueue<T extends QueueAtom> extends IHasChildQueue {
	
	/**
	 * Get the queue of {@link QueueAtom}s held by this atom/bean.
	 * 
	 * @return {@link IOLDAtomQueue} containing the queue.
	 */
	public List<T> getAtomQueue();

	/**
	 * Set the queue of {@link QueueAtom}s held by this atom/bean.
	 * 
	 * @param List of beans describing a queue.
	 */
	public void setAtomQueue(List<T> atomQueue);
	
	/**
	 * From the {@link QueueAtom}s present in the queue, calculate the 
	 * time this queue should take to run through.
	 * 
	 * @return long sum time in ms for AbstractQueueAtoms to run
	 */
	public long calculateRunTime();
	
	/**
	 * Append an {@link QueueAtom} to the end of the list comprising 
	 * this queue. Updates runtime.
	 * 
	 * @param atom - {@link QueueAtom} to add to the list.
	 * @throws IllegalArgumentException - if given atom is already in the queue.
	 * @return true if addition was successful.
	 */
	public boolean add(T atom);
	
	/**
	 * Report the length of the queue.
	 * 
	 * @return number of {@link QueueAtom}s in the queue.
	 */
	public int queueSize();
	
	/**
	 * Return the index of the {@link QueueAtom} with the given unique 
	 * ID.
	 * 
	 * @param uid - unique ID of {@link QueueAtom}.
	 * @throws IllegalArgumentException - if the supplied UID is not found.
	 * @return index of {@link QueueAtom} with given UID.
	 */
	public int getIndex(String uid);
	
	/**
	 * Return and remove the {@link QueueAtom} at the head of the queue.
	 * 
	 * @throws NoSuchElementException - if there are no items in the queue.
	 * @return {@link QueueAtom} at head of queue.
	 */
	public T next();
	
	/**
	 * Return {@link QueueAtom} at the head of the queue, without 
	 * removing from the queue. Does not change runtime.
	 * 
	 * @throws NoSuchElementException - if there are no items in the queue.
	 * @return {@link QueueAtom} at head of queue.
	 */
	public T viewNext();
	
	/**
	 * Tests whether the given {@link QueueAtom} is already present in 
	 * the queue. Used to ensure identical atoms are not added twice.
	 * 
	 * @param atom - {@link QueueAtom} whose UID is to be tested.
	 * @return true if atom already in given list.
	 */
	public boolean isAtomPresent(T atom);
	

}
