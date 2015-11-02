package org.eclipse.scanning.api.event.core;

import org.eclipse.scanning.api.event.EventException;


public interface IPublisher<T> extends ITopicConnection {
	

	/**
	 * Sends information about a specific progress using the topic to
	 * broadcast the information which is the bean (JSON encoded but if
	 * an ISubscriber is used the API user is not exposed to how the encoding
	 * works)
	 * 
	 * @param bean
	 */
	public void broadcast(T bean) throws EventException;

	
	/**
	 * Calling this method true starts a thread which notifies of
	 * 
	 * 
	 * @param alive
	 */
	public void setAlive(boolean alive) throws EventException;

	/**
	 * Returns true if the producer is alive and sending a heartbeat.
	 */
	public boolean isAlive();
}
