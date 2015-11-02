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
     * Unique id for the message.
     * @return
     */
	public String getUniqueId();
	public void setUniqueId(String uniqueId);


	/**
	 * Priority of the submission
	 * @return
	 */
	public int getPriority();
	public void setPriority(int priority);

	/**
	 * Lifetime that the event should be active for
	 * @return
	 */
	public long getLifeTime();
	public void setLifeTime(long lifeTime);


	/**
	 * Timestamp used for the event
	 * @return
	 */
	public long getTimestamp();
	public void setTimestamp(long timestamp);
}
