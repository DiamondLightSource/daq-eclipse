package org.eclipse.scanning.api.event.queues.beans;

/**
 * Base class for all bean types which exist in active-queues.
 * 
 * @author Michael Wharmby
 *
 */
public abstract class QueueAtom extends Queueable {

	/**
	 * Version ID for serialization. Should be updated when class changed. 
	 */
	private static final long serialVersionUID = 20161017L;

}
