package org.eclipse.scanning.api.event.core;

import org.eclipse.scanning.api.event.EventException;

/**
 * A submitter is a queue connection which may receive a submission on to the queue.
 * 
 * @author Matthew Gerring
 *
 * @param <T> Bean type which will be submitted.
 */
public interface ISubmitter<T> extends IQueueConnection<T> {

	/**
	 * Send a submission on to the queue.
	 * @param bean
	 */
	void submit(T bean) throws EventException;
	
	/**
	 * Send a submission on to the queue.
	 * @param bean
	 */
	void submit(T bean, boolean prepareBean) throws EventException;

	/**
	 * Send a submission on to the queue. Blocks until bean is
	 * updated with "final" status.
	 * 
	 * This method depends on this.setTopicName() already having
	 * been called with the appropriate status queue name by the
	 * user of this ISubmitter, because this method's implementation
	 * listens to the said status topic to determine when to return.
	 * 
	 * @param bean
	 * @throws EventException
	 * @throws InterruptedException
	 * @throws IllegalStateException if this.getTopicName() returns null.
	 */
	void blockingSubmit(T bean) throws EventException, InterruptedException, IllegalStateException;
	
	/**
	 * Tries to reorder the bean in the submission queue if it is
	 * still there. If the bean has been moved to the status set, 
	 * it will not be moved 
	 * 
	 * A pause will automatically be done while the bean
	 * is removed.
	 * 
	 * @param bean
	 * @return
	 * @throws EventException
	 */
	boolean reorder(T bean, int amount) throws EventException;

	/**
	 * Tries to remove the bean from the submission queue if it is
	 * still there. If the bean has been moved to the status set, 
	 * it will not be removed 
	 * 
	 * NOTE This method can end up reordering the items.
	 * A pause will automatically be done while the bean
	 * is removed.
	 * 
	 * @param bean
	 * @return
	 * @throws EventException
	 */
	boolean remove(T bean) throws EventException;
    
	/**
	 * Tries to replace the bean from the submission queue if it is
	 * still there. If the bean has been moved to the status set, 
	 * it will not be removed 
	 * 
	 * A pause will automatically be done while the bean
	 * is replace.
	 * 
	 * @param bean
	 * @return
	 * @throws EventException
	 */
	boolean replace(T bean) throws EventException;
 
	/**
     * Unique id for the message.
     * @return
     */
	String getUniqueId();
	void setUniqueId(String uniqueId);


	/**
	 * Priority of the submission
	 * @return
	 */
	int getPriority();
	void setPriority(int priority);

	/**
	 * Lifetime that the event should be active for
	 * @return
	 */
	long getLifeTime();
	void setLifeTime(long lifeTime);


	/**
	 * Timestamp used for the event
	 * @return
	 */
	long getTimestamp();
	void setTimestamp(long timestamp);
}
