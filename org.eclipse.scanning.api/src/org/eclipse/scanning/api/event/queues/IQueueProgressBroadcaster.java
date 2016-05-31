package org.eclipse.scanning.api.event.queues;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.status.Status;

/**
 * Definition of methods needed for reporting the status of a queue process 
 * (e.g. {@link IQueueProcess} or {@link IQueueProcessor}). Methods assume 
 * where a bean, which extends {@link StatusBean}, is a class field and 
 * expect some form of publisher (e.g. {@link IPublisher} where updates to this
 * bean can be broadcast.
 * 
 * @author Michael Wharmby
 *
 */
public interface IQueueProgressBroadcaster {

	/**
	 * Convenience method to call broadcast with only {@link Status} argument.
	 * 
	 * @param newStatus Status the bean has just reached.
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
		broadcast(newStatus, newPercent);
	}

	/**
	 * Broadcast the new status, update previous status, percent complete and 
	 * message of the bean associated with this process.
	 * 
	 * @param newStatus Status the bean has just reached.
	 * @param newPercent The value percent complete should be set to.
	 * @throws EventException In case broadcasting fails.
	 */
	public void broadcast(Status newStatus, Double newPercent, String message) throws EventException;
}
