package org.eclipse.scanning.api.event.servlet;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IPublisher;

/**
 * A servlet for processing a queue.
 * 
 * @author Matthew Gerring
 *
 * @param <T>
 */
public interface IConsumerServlet<T> extends IConnectable {

	
	/**
	 * Creates a process for each request processed from the queue for this servlet.
	 * 
	 * @param bean
	 * @param response
	 * @return
	 */
	public IConsumerProcess<T> createProcess(T bean, IPublisher<T> response) throws EventException;
}
