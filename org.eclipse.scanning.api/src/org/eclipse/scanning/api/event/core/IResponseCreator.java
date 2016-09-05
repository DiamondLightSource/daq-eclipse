package org.eclipse.scanning.api.event.core;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IdBean;

public interface IResponseCreator<T extends IdBean> {

	/**
	 * Called to create a responder.
	 * @param bean
	 * @param statusNotifier
	 * @return
	 * @throws EventException
	 */
	IResponseProcess<T> createResponder(T bean, IPublisher<T> statusNotifier) throws EventException;

	/**
	 * Override to allows events to be despatched asynchronously.
	 * Useful for devices that can be cancelled for instance.
	 * 
	 * @return
	 */
	default boolean isSynchronous() {
		return true;
	}
}
