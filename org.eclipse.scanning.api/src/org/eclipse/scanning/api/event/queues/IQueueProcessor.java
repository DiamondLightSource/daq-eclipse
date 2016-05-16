package org.eclipse.scanning.api.event.queues;

import org.eclipse.scanning.api.event.EventException;

public interface IQueueProcessor {
	
	void execute() throws EventException, InterruptedException;
	
	void terminate() throws EventException;

}
