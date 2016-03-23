package org.eclipse.scanning.api.event.core;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IdBean;

/**
 * 
 * A response is a class instance which deals with a single
 * request UUID
 * 
 * @author Matthew Gerring
 *
 * @param <T>
 */
public interface IResponseProcess<T extends IdBean> extends IPublishable<T> {

	/**
	 * Adds information to the request. Returns an object with
	 * the same UUID as the request.
	 * 
	 * @param request
	 * @return
	 * @throws EventException
	 */
	T process(T request) throws EventException;
}
