package org.eclipse.scanning.api.event.core;

import org.eclipse.scanning.api.event.EventException;

/**
 * The interface provided to an IConsumer which defines the work done after
 * each item is taken from the queue.
 * 
 * @author Matthew Gerring
 *
 * @param <T>
 */
public interface IProcessCreator<T> {

	IConsumerProcess<T> createProcess(T bean, IPublisher<T> statusNotifier) throws EventException;
}
