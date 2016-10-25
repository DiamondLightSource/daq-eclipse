package org.eclipse.scanning.api.event.queues.beans;

/**
 * Base class for all bean types which exist in the job-queue.
 * 
 * @author Michael Wharmby
 *
 */
public abstract class QueueBean extends Queueable {

	/**
	 * Version ID for serialization. Should be updated when class changed. 
	 */
	private static final long serialVersionUID = 20161017L;

}
