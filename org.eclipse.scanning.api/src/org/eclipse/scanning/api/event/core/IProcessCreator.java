package org.eclipse.scanning.api.event.core;

import org.eclipse.scanning.api.event.EventException;

/**
 * 
 * @author Matthew Gerring
 *
 * @param <T>
 */
public interface IProcessCreator<T> {

	IConsumerProcess<T> createProcess(T bean, IPublisher<T> statusNotifier) throws EventException;
}
