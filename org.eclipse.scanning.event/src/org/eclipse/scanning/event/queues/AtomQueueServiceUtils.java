package org.eclipse.scanning.event.queues;

import java.net.URI;
import java.util.EventListener;
import java.util.UUID;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.alive.PauseBean;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.beans.IQueueable;
import org.eclipse.scanning.api.event.status.Status;

public class AtomQueueServiceUtils {
	
	
	private static IEventService eventService = QueueServicesHolder.getEventService();
	private static IQueueService queueService = QueueServicesHolder.getQueueService();
	
	public static <T extends EventListener> ISubscriber<T> createQueueSubscriber(String queueID) throws EventException {
		URI uri = queueService.getURI();
		String topicName = queueService.getQueue(queueID).getStatusTopicName();
		
		return eventService.createSubscriber(uri, topicName);
	}
	
	public static void terminateQueueProcess(String queueID, IQueueable atomBean) throws EventException {
		//Get configuration for 
		URI uri = queueService.getURI();
		String topicName = queueService.getQueue(queueID).getStatusTopicName();
		
		atomBean.setPreviousStatus(atomBean.getPreviousStatus());
		atomBean.setStatus(Status.REQUEST_TERMINATE);
		
		IPublisher<IQueueable> terminator = eventService.createPublisher(uri, topicName);
		terminator.broadcast(atomBean);
		terminator.disconnect();
	}
	
	public static void pauseQueue(String queueID) throws EventException {
		URI uri = queueService.getURI();
		String cmdTopicName = queueService.getCommandTopicName();
		String cmdQueueName = queueService.getCommandQueueName();
		UUID consumerID = queueService.getQueue(queueID).getConsumerID();
		
		IPublisher<PauseBean> pausenator = eventService.createPublisher(uri, cmdTopicName);
		pausenator.setStatusSetName(cmdQueueName);
		PauseBean pauser = new PauseBean();
		pauser.setConsumerId(consumerID);
		pausenator.broadcast(pauser);
		pausenator.disconnect();
	}
	
}
