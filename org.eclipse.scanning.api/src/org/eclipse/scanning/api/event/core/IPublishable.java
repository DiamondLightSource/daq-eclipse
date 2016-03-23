package org.eclipse.scanning.api.event.core;

/**
 * A process, third party software run or response which happens at a specific time.
 * 
 * @author Matthew Gerring
 *
 * @param <T>
 */
public interface IPublishable<T> {

	/**
	 *  
	 * @return the bean which this process is currently running.
	 */
	T getBean();
	
	/**
	 *  
	 * @return the bean which this process is currently running.
	 */
	IPublisher<T> getPublisher();

}
