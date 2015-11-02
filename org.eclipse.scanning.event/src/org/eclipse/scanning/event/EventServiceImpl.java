package org.eclipse.scanning.event;

import java.net.URI;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventListener;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;

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

	@SuppressWarnings("rawtypes")
	@Override
	public <T extends IEventListener> ISubscriber<T> createSubscriber(URI uri, String topicName) {
		return createSubscriber(uri, topicName, eventConnectorService);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public <T extends IEventListener> ISubscriber<T> createSubscriber(URI uri, String topic, IEventConnectorService service) {
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
	public <U> ISubmitter<U> createSubmitter(URI uri, String queueName) {
		return createSubmitter(uri, SUBMISSION_QUEUE, null);
	}

	@Override
	public <U> ISubmitter<U> createSubmitter(URI uri, String queueName, IEventConnectorService service) {
		if (service == null) service = eventConnectorService;
		return new SubmissionImpl<U>(uri, queueName, service);
	}

	@Override
	public <U> IConsumer<U> createConsumer(URI uri) throws EventException {
		return createConsumer(uri, SUBMISSION_QUEUE, STATUS_QUEUE, STATUS_TOPIC, HEARTBEAT_TOPIC, TERMINATE_TOPIC, null);
	}

	@Override
	public <U> IConsumer<U> createConsumer(URI uri, 
			                               String submissionQName,
			                               String statusQName, 
			                               String statusTName, 
			                               String heartbeatTName,
			                               String terminateTName,
			                               IEventConnectorService service) throws EventException {
		
		if (service == null) service = eventConnectorService;
		
		return new ConsumerImpl<U>(uri, submissionQName, statusQName, statusTName, heartbeatTName, terminateTName, service, this);

	}
}
