package org.eclipse.scanning.event;

import java.net.URI;
import java.util.EventListener;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.status.StatusBean;

public class EventServiceImpl implements IEventService {
	
	static {
		System.out.println("Started "+IEventService.class.getSimpleName());
	}
	
	private static IEventConnectorService eventConnectorService;

	public static IEventConnectorService getEventConnectorService() {
		return eventConnectorService;
	}

	public static void setEventConnectorService(IEventConnectorService eventService) {
		EventServiceImpl.eventConnectorService = eventService;
	}

	@Override
	public <T extends EventListener> ISubscriber<T> createSubscriber(URI uri, String topicName) {
		return createSubscriber(uri, topicName, eventConnectorService);
	}

	@Override
	public <T extends EventListener> ISubscriber<T> createSubscriber(URI uri, String topic, IEventConnectorService service) {
		if (service == null) service = eventConnectorService;
		return new SubscriberImpl<T>(uri, topic, service);
	}
	
	
	@Override
	public <U> IPublisher<U> createPublisher(URI uri, String topicName) {
		return createPublisher(uri, topicName, eventConnectorService);
	}
	
	@Override
	public <U> IPublisher<U> createPublisher(URI uri, String topic, IEventConnectorService service) {
		if (service == null) service = eventConnectorService;
		return new PublisherImpl<U>(uri, topic, service);
	}

	@Override
	public <U extends StatusBean> ISubmitter<U> createSubmitter(URI uri, String queueName) {
		return createSubmitter(uri, queueName, null);
	}

	@Override
	public <U extends StatusBean> ISubmitter<U> createSubmitter(URI uri, String queueName, IEventConnectorService service) {
		if (service == null) service = eventConnectorService;
		return new SubmitterImpl<U>(uri, queueName, service);
	}

	@Override
	public <U extends StatusBean> IConsumer<U> createConsumer(URI uri) throws EventException {
		return createConsumer(uri, SUBMISSION_QUEUE, STATUS_SET, STATUS_TOPIC, HEARTBEAT_TOPIC, KILL_TOPIC, null);
	}
	
	@Override
	public <U extends StatusBean> IConsumer<U> createConsumer(URI uri, 
			                               String submissionQName,
			                               String statusQName, 
			                               String statusTName, 
			                               IEventConnectorService service) throws EventException {
		return createConsumer(uri, submissionQName, statusQName, statusTName, HEARTBEAT_TOPIC, KILL_TOPIC, service);
	}
	

	@Override
	public <U extends StatusBean> IConsumer<U> createConsumer(URI uri, 
			                               String submissionQName,
			                               String statusQName, 
			                               String statusTName, 
			                               String heartbeatTName,
			                               String killTName,
			                               IEventConnectorService service) throws EventException {
		
		if (service == null) service = eventConnectorService;
		
		return new ConsumerImpl<U>(uri, submissionQName, statusQName, statusTName, heartbeatTName, killTName, service, this);

	}
}
