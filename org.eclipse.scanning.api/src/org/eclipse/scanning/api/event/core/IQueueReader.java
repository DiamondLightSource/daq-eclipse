package org.eclipse.scanning.api.event.core;

import java.util.List;

import org.eclipse.scanning.api.event.EventException;

public interface IQueueReader<T> extends IURIConnection, IBeanClass<T> {

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
