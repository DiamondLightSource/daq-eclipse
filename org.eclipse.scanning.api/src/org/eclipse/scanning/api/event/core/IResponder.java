package org.eclipse.scanning.api.event.core;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IdBean;

/**
 * 
 * A responder is an object that can respond to a topic posted by a poster.
 * One or more responders may be registered and they post for a given request
 * to the response topic. 
 * 
 * The implementation of IPoster blocks until it thinks (via timeout) all responses
 * are in then collates them (depending on response object, for instance detectors)
 * and returns.
 * 
 * @author Matthew Gerring
 *
 */
public interface IResponder<T extends IdBean> extends IRequestResponseConnection, IBeanClass<T>{

	/**
	 * The responder will be asked to respond to posts on the request topic
	 * of this object type.
	 * 
	 * @param responder
	 * @throws EventException
	 */
	void setResponseCreator(IResponseCreator<T> responder) throws EventException;
	
}
