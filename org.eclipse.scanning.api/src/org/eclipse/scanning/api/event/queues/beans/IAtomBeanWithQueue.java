package org.eclipse.scanning.api.event.queues.beans;

/**
 * Interface to enforce the composition rather than inheritance relationship 
 * of IAtomQueues within beans.
 * 
 * @author Michael Wharmby
 *
 * @param <T> Class extending QueueAtom; this class will be held in the Queue
 *            inside this Atom/Bean
 */
public interface IAtomBeanWithQueue<T extends QueueAtom> extends IQueueable {
	
	/**
	 * Get the queue of {@link QueueAtom}s held by this atom/bean.
	 * 
	 * @return {@link IAtomQueue} containing the queue.
	 */
	public IAtomQueue<T> getAtomQueue();

	/**
	 * Set the queue of {@link QueueAtom}s held by this atom/bean.
	 * 
	 * @param atomQueue {@link IAtomQueue} describing a queue.
	 */
	public void setAtomQueue(IAtomQueue<T> atomQueue);
	
	/**
	 * Convenience method to determine the runtime of the queue.
	 * 
	 * @return long number of ms for queue to run
	 */
	public default long runTime() {
		return getAtomQueue().getRunTime();
	}
	
	/**
	 * Convenience method to allow easier interaction with the queue.
	 * 
	 * @return {@link IAtomQueue} containing representing the queue.
	 */
	public default IAtomQueue<T> queue() {
		return getAtomQueue();
	}

}
