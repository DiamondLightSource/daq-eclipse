package org.eclipse.scanning.api.event.core;

import java.util.List;

import org.eclipse.scanning.api.event.EventException;

public interface IQueueReader<T> extends IURIConnection {

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
	 * The queue of beans type T, unordered
	 * @return
	 * @throws EventException
	 */
	public List<T> getQueue() throws EventException;
	
	
	/**
	 * Call to disconnect all publishers and subscribers when the connection goes down.
	 * @throws EventException
	 */
	public void disconnect() throws EventException;


}
