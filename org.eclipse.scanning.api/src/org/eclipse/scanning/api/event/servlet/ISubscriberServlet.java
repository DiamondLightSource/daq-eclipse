package org.eclipse.scanning.api.event.servlet;

import java.net.URISyntaxException;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;

/**
 * This interface is used to mark extensions to the acquisition server
 * which may be made to expose certain services. It is like remote OSGi
 * services, however the client and the server do not have to be Java,
 * more like a servlet. For instance the client can be VM running a scan
 * for B23 or it can be a javascript client like syncweb.
 * 
 * The event servlet in spring must have its uri and topic set before
 * the connect() method is called.
 * 
 * @author Matthew Gerring
 *
 * @param <T>
 */
public interface ISubscriberServlet<T> extends IConnectable {
	

	/**
	 * Called to do work in the servlet. 
	 * 
	 * @param bean
	 * @throws EventException
	 */
	void doObject(T bean, IPublisher<T> response) throws EventException;

}
