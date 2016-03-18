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
	 * Tries to remove the bean from the set
	 * 
	 * NOTE This method can end up reordering the items - only use on queues
	 * which are being used like Sets, not queues like submission queues
	 * in which order matters.
	 * 
	 * @param bean
	 * @return
	 * @throws EventException
	 */
	boolean remove(T bean) throws EventException;
    
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
