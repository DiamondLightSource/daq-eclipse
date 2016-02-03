package org.eclipse.scanning.api.event.servlet;

import java.net.URISyntaxException;

import org.eclipse.scanning.api.event.EventException;

public interface IConnectable {
	/**
	 * Should called to start the servlet.
	 * @param uri, a string representation of the activemq uri.
	 */
	public void connect() throws EventException, URISyntaxException;

	/**
	 * Should called to stop the servlet but if it is not called
	 * the servlet will run the lifetime of the server.
	 * 
	 * This is acceptable if it is a service client(s) may demand at
	 * any time.
	 */
	public void disconnect() throws EventException;

}
