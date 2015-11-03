package org.eclipse.scanning.api.event.core;

import org.eclipse.scanning.api.event.EventException;

public interface ITopicConnection extends IURIConnection {
	
	/**
	 * The string topic to publish events on for this manager.
	 * The events will be beans which serialize to JSON.
	 * 
	 * @return
	 */
	public String getTopicName();
	
	/**
	 * Sets the scan topic, causing the connection to be made to that topic and throwing an
	 * exception if the connection cannot be made.
	 * @param topic
	 * @throws EventException
	 */
	public void setTopicName(String topic) throws EventException;

	
	/**
	 * Call to disconnect all publishers and subscribers when the connection goes down.
	 * @throws EventException
	 */
	public void disconnect() throws EventException;

}
