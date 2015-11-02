package org.eclipse.scanning.api.event.core;

import org.eclipse.scanning.api.event.EventException;

public interface IQueueConnection<T> {
	
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

}
