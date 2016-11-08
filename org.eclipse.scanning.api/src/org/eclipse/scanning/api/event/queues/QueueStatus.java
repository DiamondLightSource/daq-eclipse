package org.eclipse.scanning.api.event.queues;

/**
 * Current state of a queue managed within the {@link IQueueService}.
 * 
 * @author Michael Wharmby
 *
 */
public enum QueueStatus {
	INITIALISED, STARTED, STOPPING, STOPPED, KILLED, DISPOSED;
	
	/**
	 * Return whether the queue is running
	 * 
	 * @return true if running.
	 */
	public boolean isActive() {
		return this == STARTED;
	}
	
	/**
	 * Return whether the queue is in a startable state
	 * 
	 * @return true if can be started.
	 */
	public boolean isStartable() {
		return this == INITIALISED || this == STOPPED;
	}

}
