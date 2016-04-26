package org.eclipse.scanning.event;

import java.net.URI;
import java.util.EventListener;

import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IResponder;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.status.StatusBean;

public class EventServiceImpl implements IEventService {
	
	static {
		System.out.println("Started "+IEventService.class.getSimpleName());
	}
	
	private static IEventConnectorService eventConnectorService;
	
	public EventServiceImpl() {
		
	}
	
	// For tests
	public EventServiceImpl(IEventConnectorService serviceToUse) {
		eventConnectorService = serviceToUse;
		if (eventConnectorService==null) throw new NullPointerException("No '"+IEventConnectorService.class.getSimpleName()+"' was found!");
	}

	public static void setEventConnectorService(IEventConnectorService eventService) {
		EventServiceImpl.eventConnectorService = eventService;
	}

	@Override
	public <T extends EventListener> ISubscriber<T> createSubscriber(URI uri, String topicName) {
		return new SubscriberImpl<T>(uri, topicName, eventConnectorService);
	}
	
	
	@Override
	public <U> IPublisher<U> createPublisher(URI uri, String topicName) {
		return new PublisherImpl<U>(uri, topicName, eventConnectorService);
	}

	@Override
	public <U extends StatusBean> ISubmitter<U> createSubmitter(URI uri, String queueName) {
		return new SubmitterImpl<U>(uri, queueName, eventConnectorService, this);
	}

	@Override
	public <U extends StatusBean> IConsumer<U> createConsumer(URI uri) throws EventException {
		return createConsumer(uri, SUBMISSION_QUEUE, STATUS_SET, STATUS_TOPIC, HEARTBEAT_TOPIC, CMD_TOPIC);
	}
	
	@Override
	public <U extends StatusBean> IConsumer<U> createConsumer(URI uri, 
			                               String submissionQName,
			                               String statusQName, 
			                               String statusTName) throws EventException {
		return createConsumer(uri, submissionQName, statusQName, statusTName, HEARTBEAT_TOPIC, CMD_TOPIC);
	}
	

	@Override
	public <U extends StatusBean> IConsumer<U> createConsumer(URI uri, 
			                               String submissionQName,
			                               String statusQName, 
			                               String statusTName, 
			                               String heartbeatTName,
			                               String commandTName) throws EventException {
				
		return new ConsumerImpl<U>(uri, submissionQName, statusQName, statusTName, heartbeatTName, commandTName, eventConnectorService, this);

	}

	@Override
	public void checkHeartbeat(URI uri, String patientName, long listenTime) throws EventException, InterruptedException {
		HeartbeatChecker checker = new HeartbeatChecker(this, uri, patientName, listenTime);
		checker.checkPulse();
	}

	@Override
	public <T extends INameable> void checkTopic(URI uri, String patientName, long listenTime, String topicName, Class<T> beanClass) throws EventException, InterruptedException {
		TopicChecker<T> checker = new TopicChecker<T>(this, uri, patientName, listenTime, topicName, beanClass);
		checker.checkPulse();
	}

	@Override
	public <T extends IdBean> IRequester<T> createRequestor(URI uri, String requestTopic, String responseTopic) throws EventException {
		return new RequesterImpl<T>(uri, requestTopic, responseTopic, this);
	}

	@Override
	public <T extends IdBean> IResponder<T> createResponder(URI uri, String requestTopic, String responseTopic) throws EventException {
		return new ResponderImpl<T>(uri, requestTopic, responseTopic, this);
	}
	
	@Override
	public IEventConnectorService getEventConnectorService() {
		return eventConnectorService;
	}

}
