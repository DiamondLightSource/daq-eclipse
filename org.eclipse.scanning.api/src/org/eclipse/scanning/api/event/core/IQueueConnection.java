package org.eclipse.scanning.api.event.core;

import java.util.List;

import org.eclipse.scanning.api.event.EventException;

public interface IQueueConnection<T> extends IURIConnection {
	
	/**
	 * The string to define the queue for storing status of scans.
	 * 
	 * @return
	 */
	public String getStatusSetName();
	
	/**
	 * The string to define the queue for storing status of scans.
	 * @param topic
	 * @throws EventException
	 */
	public void setStatusSetName(String queueName) throws EventException;

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
	 * This method will read a queue or a set from ActiveMQ into a list.
	 * @param queueName
	 * @param fieldName
	 *   If field is set, it will be used to order the beans in the list by making a comparitor using it.
	 */
	public List<T> getQueue(String queueName, String fieldName) throws EventException;
	
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

	/**
	 * Class of bean usually extending StatusBean
	 * 
	 * @return class or null
	 */
	public Class<T> getBeanClass();

	/**
	 * Class of bean usually extending StatusBean
	 * 
	 * It is not compulsory to set the bean class unless trying to deserialize messages sent by older versions of the connector service.
	 */
	public void setBeanClass(Class<T> beanClass);

	/**
	 * Attempts to reorder the bean in the submission queue. This is only 
	 * possible if the bean is 
	 * 
	 * @param selection
	 * @param amount (e.g. +1 more urgent, -1 less urgent)
	 * @throws EventException if there was an issue with the required position in the queue.
	 */
	void reorder(T bean, int amount) throws EventException;

}
