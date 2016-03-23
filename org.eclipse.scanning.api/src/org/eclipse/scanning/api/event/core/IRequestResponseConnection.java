package org.eclipse.scanning.api.event.core;

import org.eclipse.scanning.api.event.EventException;

/**
 * 
 * @author Matthew Gerring
 *
 */
public interface IRequestResponseConnection extends IURIConnection {

	/**
	 * The topic used to request the response
	 */
	void setRequestTopic(String requestTopic);
	
	/**
	 * The topic used to request the response
	 * @return
	 */
	String getRequestTopic();
	
	/**
	 * The topic used to send the response.
	 */
	void setResponseTopic(String responseTopic);
	
	/**
	 * The topic used to send the response.
	 * @return
	 */
	String getResponseTopic();
	
	/**
	 * Call to disconnect
	 * @throws EventException
	 */
	public void disconnect() throws EventException;

}
