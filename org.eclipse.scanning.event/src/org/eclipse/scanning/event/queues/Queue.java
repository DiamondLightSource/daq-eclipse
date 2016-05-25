package org.eclipse.scanning.event.queues;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.queues.IHeartbeatMonitor;
import org.eclipse.scanning.api.event.queues.IQueue;
import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.QueueStatus;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Queue is a concrete implementation of {@link IQueue}. It holds details of 
 * the consumer and of the consumer/queue configuration needed for the 
 * {@link IQueueService} to control the queue.
 * 
 * @author Michael Wharmby
 *
 * @param <T> Base type of atom/bean operated on by the queue, e.g. 
 *            {@link QueueAtom} or {@QueueBean}.
 */
public class Queue<T extends Queueable> implements IQueue<T> {
	
	public static final String SUBMISSION_QUEUE_KEY = "submitQ";
	public static final String SUBMISSION_QUEUE_SUFFIX = ".submission.queue";
	public static final String STATUS_QUEUE_KEY = "statusQ";
	public static final String STATUS_QUEUE_SUFFIX = ".status.queue";
	public static final String STATUS_TOPIC_KEY = "statusT";
	public static final String STATUS_TOPIC_SUFFIX = ".status.topic";
	public static final String HEARTBEAT_TOPIC_KEY = "heartbeatT";
	public static final String HEARTBEAT_TOPIC_SUFFIX = ".heartbeat.topic";
	public static final String COMMAND_TOPIC_KEY = "commandT";
	public static final String COMMAND_TOPIC_SUFFIX = ".command.topic";
	
	private static final Logger logger = LoggerFactory.getLogger(Queue.class);
	
	private final IConsumer<T> consumer;
	private final IHeartbeatMonitor heartMonitor;
	private final Map<String, String> queueNames;
	private final String queueID;
	private QueueStatus queueStatus;
	
	public Queue(String qID, URI uri) throws EventException {
		this(qID, qID+HEARTBEAT_TOPIC_SUFFIX, qID+COMMAND_TOPIC_SUFFIX, uri);
	}
	
	public Queue(String qID, String heartbeatTopic, String commandTopic, URI uri) throws EventException {
		this.queueID = qID;
		queueNames = new HashMap<>(5);
		queueNames.put(SUBMISSION_QUEUE_KEY, queueID+SUBMISSION_QUEUE_SUFFIX);
		queueNames.put(STATUS_QUEUE_KEY, queueID+STATUS_QUEUE_SUFFIX);
		queueNames.put(STATUS_TOPIC_KEY, queueID+STATUS_TOPIC_SUFFIX);
		queueNames.put(HEARTBEAT_TOPIC_KEY, heartbeatTopic);
		queueNames.put(COMMAND_TOPIC_KEY, commandTopic);
		
		IEventService eventService = QueueServicesHolder.getEventService();
		
		try {
			consumer = eventService.createConsumer(uri, getSubmissionQueueName(),
					getStatusQueueName(), getStatusTopicName(), getHeartbeatTopicName(),
					getCommandTopicName());
			consumer.setRunner(new QueueProcessCreator<T>(true));
		} catch (EventException eEx) {
			logger.error("Failed to create consumer for "+queueID+".");
			throw new EventException(eEx);
		}
		
		//This must be called after the consumer has been created.
		try {
			heartMonitor = new HeartbeatMonitor(uri, this, true);
		} catch (EventException eEx) {
			logger.error("Failed to create HeartbeatMonitor for "+queueID+".");
			throw new EventException(eEx);
		}
		
		queueStatus = QueueStatus.INITIALISED;
	}
	
	@Override
	public String getQueueID() {
		return queueID;
	}

	@Override
	public QueueStatus getQueueStatus() {
		return queueStatus;
	}

	@Override
	public void setQueueStatus(QueueStatus status) {
		this.queueStatus = status;
	}
	
	@Override
	public Map<String, String> getQueueNames() {
		return queueNames;
	}
	
	@Override
	public String getSubmissionQueueName() {
		return queueNames.get(SUBMISSION_QUEUE_KEY);
	}

	@Override
	public String getStatusQueueName() {
		return queueNames.get(STATUS_QUEUE_KEY);
	}

	@Override
	public String getStatusTopicName() {
		return queueNames.get(STATUS_TOPIC_KEY);
	}

	@Override
	public String getHeartbeatTopicName() {
		return queueNames.get(HEARTBEAT_TOPIC_KEY);
	}

	@Override
	public String getCommandTopicName() {
		return queueNames.get(COMMAND_TOPIC_KEY);
	}
	
	@Override
	public IConsumer<T> getConsumer() {
		return consumer;
	}
	
	@Override
	public IHeartbeatMonitor getHeartbeatMonitor() {
		return heartMonitor;
	}
	
	@Override
	public boolean clearQueues() throws EventException {
		consumer.clearQueue(getSubmissionQueueName());
		consumer.clearQueue(getStatusQueueName());
		
		if (consumer.getStatusSet().size() == 0 && consumer.getSubmissionQueue().size() == 0) return true;
		else return false;
	}
	
	@Override
	public void disconnect() throws EventException {
		heartMonitor.disconnect();
		consumer.disconnect();
	}
	
	@Override
	public boolean hasSubmittedJobsPending() throws EventException {
		return !consumer.getSubmissionQueue().isEmpty();
	}

	

}
