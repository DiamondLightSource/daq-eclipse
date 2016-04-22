package org.eclipse.scanning.api.event.queues.beans;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.eclipse.scanning.api.event.queues.IQueueService;

/**
 * An IAtomQueue allows the queueing of a set of beans implementing the 
 * {@link QueueAtom} abstract class. It is assumed classes implementing 
 * IAtomQueue also extend {@link Queueable}. Thus, queues within these beans,
 * when consumed by consumers within the {@link IQueueService} will have their 
 * contents spooled into a new sub-queue.
 * 
 * @author Michael Wharmby
 *
 */

public interface IAtomQueue<T extends QueueAtom> extends IAtomWithChildQueue {
	
	/**
	 * Returns list of {@link QueueAtom} which are present in this 
	 * queue.
	 * 
	 * @return List {@link QueueAtom}s in the queue.
	 */
	public List<T> getQueue();
	
	/**
	 * Change the current queue to the given list.
	 * 
	 * @param queue list of {@link QueueAtom} representing new queue.
	 */
	public void setQueue(List<T> queue);
	
	/**
	 * Get the amount of time (in ms) necessary to complete the processes in 
	 * this IAtomQueue.
	 * 
	 * @return long number of ms to complete processes.
	 */
	public long getRunTime();
	
	/**
	 * Update the expected queue run time to the given value (in ms).
	 * 
	 * @param runTime long representing time (in ms) for queue to run.
	 */
	public void setRunTime(long runTime);
	
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
	 * Insert an {@link QueueAtom} to the list comprising this queue at 
	 * ith position. Updates runtime.
	 * 
	 * @param atom - the {@link QueueAtom} to add to the list.
	 * @param index - index at which to insert the {@link QueueAtom}.
	 * @throws IllegalArgumentException - if given atom is already in the queue.
	 * @throws IndexOutOfBoundsException - if the index is out of range.
	 * @return true if insertion was successful.
	 */
	public boolean add(T atom, int index);
	
	/**
	 * Append a Collection of {@link QueueAtom}s to the list comprising 
	 * this queue. Updates runtime.
	 * 
	 * @param queue - Collection of {@link QueueAtom}s to be appended.
	 * @throws IllegalArgumentException - if atom(s) in given list are already
	 * 									  in the queue.
	 * @throws NullPointerException - if the supplied Collection is null.
	 * @return true if addition was successful.
	 */
	public boolean addList(Collection<T> atomList);
	
	/**
	 * Insert a Collection of {@link QueueAtom} to the list comprising 
	 * this queue at the ith position. Updates runtime.
	 * 
	 * @param queue - Collection of {@link QueueAtom}s to be appended.
	 * @param index - index at which to insert the Collection of 
	 *				  {@link QueueAtom}s.
	 * @throws IllegalArgumentException - if atom(s) in given list are already
	 * 									  in the queue.
	 * @throws IndexOutOfBoundsException - if the index is out of range.
	 * @throws NullPointerException - if the supplied Collection is null.
	 * @return true if successful.
	 */
	public boolean addList(Collection<T> atomList, int index);
	
	/**
	 * Remove & return {@link QueueAtom} at index i from list 
	 * comprising this queue. Updates runtime.
	 * 
	 * @param index of {@link QueueAtom} to remove.
	 * @throws IndexOutOfBoundsException - if the index is out of range.
	 * @return true if removal successful.
	 */
	public boolean remove(int index);
	
	/**
	 * Remove and return {@link QueueAtom} with name from the list 
	 * comprising this scan. Updates runtime.
	 * 
	 * @param uid - unique ID of {@link QueueAtom} to be removed.
	 * @throws IllegalArgumentException - if the supplied UID is not found.
	 * @return true if removal successful.
	 */
	public boolean remove(String uid);
	
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
	 * Return the {@link QueueAtom} at the given index, without 
	 * removing it from the queue.
	 * 
	 * @param index - position of {@link QueueAtom} to return.
	 * @throws IndexOutOfBoundsException - if index is out of range.
	 * @return {@link QueueAtom} from requested queue position.
	 */
	public T view(int index);
	
	/**
	 * Return the {@link QueueAtom} with a given unique ID, without 
	 * removing from the queue.
	 * 
	 * @param uid - unique ID of {@link QueueAtom}.
	 * @throws IllegalArgumentException - if the supplied UID is not found.
	 * @return {@link QueueAtom} with given unique ID.
	 */
	public T view(String uid);
	
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
	 * Return {@link QueueAtom} at the tail of the queue. Does not 
	 * change runtime.
	 * 
	 * @throws NoSuchElementException - if there are no items in the queue.
	 * @return {@link QueueAtom} at the tail of queue.
	 */
	public T last();
	
	/**
	 * Return {@link QueueAtom} at the tail of the queue, without 
	 * removing from the queue.
	 * 
	 * @throws NoSuchElementException - if there are no items in the queue.
	 * @return {@link QueueAtom} at the tail of queue.
	 */
	public T viewLast();
	
	/**
	 * Returns the {@link ListIterator} for the queue.
	 * 
	 * @return ListIterator of {@link QueueAtom}s in queue.
	 */
	public ListIterator<T> getQueueIterator();
	
	/**
	 * Tests whether the given {@link QueueAtom} is already present in 
	 * the queue. Used to ensure identical atoms are not added twice.
	 * 
	 * @param atom - {@link QueueAtom} whose UID is to be tested.
	 * @return true if atom already in given list.
	 */
	public boolean isAtomPresent(T atom);
	
	/**
	 * Tests whether an {@link QueueAtom} in the given Collection is 
	 * already present in the queue. Used to ensure identical atoms are not 
	 * added twice.
	 * 
	 * @param atomSet - Collection of {@link QueueAtom} whose UIDs are
	 * 					to be tested.
	 * @return true if atom already in given list.
	 */
	public boolean isAtomInListPresent(Collection<T> atomSet);
	
	/**
	 * Calculate unique hash for this object.
	 * 
	 * @return int Hash of this object.
	 */
	public int hashCode();
	
	/**
	 * Detemine whether this IAtomQueue is equal to another object.
	 * 
	 * @param obj to be compared with this instance.
	 * @return true if obj and this instance are the same.
	 */
	public boolean equals(Object obj);

}
