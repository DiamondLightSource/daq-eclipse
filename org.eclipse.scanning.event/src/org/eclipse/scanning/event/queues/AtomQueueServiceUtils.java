package org.eclipse.scanning.event.queues;

import java.net.URI;
import java.util.EventListener;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.queues.IQueueService;

public class AtomQueueServiceUtils {
	
	private static IEventService eventService = QueueServicesHolder.getEventService();
	private static IQueueService queueService = QueueServicesHolder.getQueueService();
	
	public static <T extends EventListener> ISubscriber<T> createActiveQueueSubscriber(String queueID) throws EventException {
		URI uri = queueService.getURI();
		String topicName = queueService.getActiveQueue(queueID).getStatusTopicName();
		
		return eventService.createSubscriber(uri, topicName);
	}

}
