package org.eclipse.scanning.api.event.queues;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.status.Status;

/**
 * Methods for broadcasting the status and percentage complete of a bean within a queue. It is assumed that the bean is configured already and that some method of broadcasting is  
 * 
 * @author Michael Wharmby
 *
 */
public interface IQueueBroadcaster<T> {
	/**
	 * Convenience method to call broadcast with both {@link Status} and 
	 * message arguments.
	 * 
	 * @param newStatus {@link Status} the bean has just reached.
	 * @param message String to message to publish on the bean.
	 * @throws EventException In case broadcasting fails.
	 */
	public default void broadcast(Status newStatus, String message) throws EventException {
		broadcast(newStatus, null, message);
	}

	/**
	 * Convenience method to call broadcast with only {@link Status} argument.
	 * 
	 * @param newStatus {@link Status} the bean has just reached.
	 * @throws EventException In case broadcasting fails.
	 */
	public default void broadcast(Status newStatus) throws EventException {
		broadcast(newStatus, null, null);
	}

	/**
	 * Convenience method to call broadcast with only percent complete 
	 * argument.
	 * 
	 * @param newPercent The value percent complete should be set to.
	 * @throws EventException In case broadcasting fails.
	 */
	public default void broadcast(double newPercent) throws EventException {
		broadcast(null, newPercent, null);
	}

	/**
	 * Convenience method to call broadcast with percent complete and 
	 * {@link Status} arguments.
	 * 
	 * @param newStatus Status the bean has just reached.
	 * @param newPercent The value percent complete should be set to.
	 * @throws EventException In case broadcasting fails.
	 */
	public default void broadcast(Status newStatus, Double newPercent) throws EventException {
		broadcast(newStatus, newPercent, null);
	}

	/**
	 * Broadcast the new status, update previous status, percent complete and 
	 * message of the bean associated with this process.
	 * 
	 * @param newStatus {@link Status} the bean has just reached.
	 * @param newPercent The value percent complete should be set to.
	 * @param message String to message to publish on the bean.
	 * @throws EventException In case broadcasting fails.
	 */
	public void broadcast(Status newStatus, Double newPercent, String message) throws EventException;
	
	/**
	 * Broadcast the bean when some interaction of the child queue (e.g. from 
	 * {@link QueueListener}) has updated its status/percent complete/message.
	 * 
	 * @throws EventException In case broadcasting fails.
	 */
	public void childQueueBroadcast() throws EventException;
	
	/**
	 * Return the IPublisher instance used to broadcast the bean status.
	 * 
	 * @return IPublisher for bean broadcasting.
	 */
	public IPublisher<T> getPublisher();
	
	/**
	 * Return the bean whose status updates will be broadcast.
	 * 
	 * @return T Bean to be broadcast
	 */
	public T getBean();
}
