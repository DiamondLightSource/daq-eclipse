package org.eclipse.scanning.api.event.core;

import java.util.List;

import org.eclipse.scanning.api.event.EventException;

public interface IQueueConnection<T> extends IURIConnection {
	
	/**
	 * The string to define the queue for storing status of scans.
	 * 
	 * @return
	 */
	public String getStatusQueueName();
	
	/**
	 * The string to define the queue for storing status of scans.
	 * @param topic
	 * @throws EventException
	 */
	public void setStatusQueueName(String queueName) throws EventException;

	/**
	 * The string to define the queue for submitting scan objects to.
	 * 
	 * @return
	 */
	public String getSubmitQueueName();
	
	/**
	 * The string to define the queue for submitting scan objects to.
	 * @throws EventException
	 */
	public void setSubmitQueueName(String queueName) throws EventException;
	
	/**
	 * Call to disconnect all publishers and subscribers when the connection goes down.
	 * @throws EventException
	 */
	public void disconnect() throws EventException;

	/**
	 * This method will read a queue
	 */
	public List<T> getQueue(String queueName) throws EventException;

	/**
	 * This method will purge the queue
	 * USE WITH CAUTION
	 */
	public void clearQueue(String queueName) throws EventException;

	/**
	 * Used to massage the status queue when a consumer starts up for instance.
	 * It removes very old runs or those which are in a final failed state.
	 */
	public void cleanQueue(String queueName) throws EventException;

}
