package org.eclipse.scanning.api.event.queues;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.beans.Queueable;

public interface IQueueProcessor {
	
	public <T extends Queueable> IConsumerProcess<T> makeProcess(T bean,
			IPublisher<T> publisher, boolean blocking) throws EventException;

}
