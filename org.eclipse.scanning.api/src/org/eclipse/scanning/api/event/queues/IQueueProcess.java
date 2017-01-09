package org.eclipse.scanning.api.event.queues;

import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.queues.beans.Queueable;

/**
 * Process used in the queue service to run the instructions provided by a 
 * queue bean. By separating the process and the processor, it is possible to 
 * configure the behaviour of the process at runtime, allowing processing of 
 * beans from a heterogenous queue. 
 * 
 * @author Michael Wharmby
 *
 * @param <Q> Bean type that will be operated on
 *
 * @param <T> TODO CHANGEME Bean type passed by consumer (this might be a super-type of the 
 *            bean to be processed in the case of heterogenous queues).
 */
public interface IQueueProcess <Q extends Queueable, T extends Queueable> extends IConsumerProcess<T> {

	/**
	 * Returns the bean which will be operated on by this process.
	 * 
	 * @return P bean to be processed.
	 */
	public Q getQueueBean();
	
	/**
	 * Return whether execution has begun.
	 * 
	 * @return true if execution begun.
	 */
	public boolean isExecuted();

	/**
	 * Return whether the process has been terminated.
	 * 
	 * @return true if has been terminated.
	 */
	public boolean isTerminated();
	
	/**
	 * Return the class of the bean which this IQueueProcessor can process.
	 * 
	 * @return Class of bean which can be processed.
	 */
	public Class<Q> getBeanClass();

}
