package org.eclipse.scanning.api.event.servlet;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IResponseProcess;

/**
 * A servlet for processing a queue.
 * 
 * @author Matthew Gerring
 *
 * @param <T>
 */
public interface IResponderServlet<T extends IdBean> extends IConnectable {

	
	/**
	 * Creates a response for each request processedfor this topic.
	 * 
	 * @param bean
	 * @param response
	 * @return
	 */
	public IResponseProcess<T> createResponder(T bean, IPublisher<T> response) throws EventException;
}
