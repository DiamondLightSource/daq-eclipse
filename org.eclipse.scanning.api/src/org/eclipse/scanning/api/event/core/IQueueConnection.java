package org.eclipse.scanning.api.event.core;

import java.util.List;
import java.util.Map;

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
	 * Class of bean usually extending StatusBean. An alternative
	 * where the class of the bean is variable for a given queue is
	 * to set the string fields 'bundle' and 'beanClass' in the bean.
	 * The system will then look to see if these strings are set in the
	 * json and attempt to deserialize using these.
	 * 
	 * @return class or null
	 */
	public Class<T> getBeanClass();

	/**
	 * Class of bean usually extending StatusBean. An alternative
	 * where the class of the bean is variable for a given queue is
	 * to set the string fields 'bundle' and 'beanClass' in the bean.
	 * The system will then look to see if these strings are set in the
	 * serialized string and attempt to deserialize using these.
	 * 
	 * @return
	 */
	public void setBeanClass(Class<T> beanClass) throws EventException;

}
