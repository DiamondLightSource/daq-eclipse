package org.eclipse.scanning.api.event.core;

import org.eclipse.scanning.api.event.EventException;


/**
 * A consumer process is the process run when a consumer starts a job.
 * ProgressibleProcess in the DAWN command server plugin is the inspiration for this plugin.
 * 
 * @author Matthew Gerring
 *
 */
public interface IConsumerProcess<T> {

	/**
	 *  
	 * @return the bean which this process is currently running.
	 */
	public T getBean();
	
	/**
	 *  
	 * @return the bean which this process is currently running.
	 */
	public IPublisher<T> getPublisher();
	
	/**
	 * Execute the process, if an exception is thrown the process is set to 
	 * failed and the message is the message of the exception.
	 * 
	 * @throws Exception
	 */
	public void execute() throws EventException;
	
	/**
	 * Please provide a termination for the process by implementing this method.
	 * If the process has a stop file, write it now; if it needs to be killed,
	 * get its pid and kill it; if it is running on a cluster, use the qdel or dramaa api.
	 * 
	 * @throws Exception
	 */
	public void terminate() throws EventException;

}
