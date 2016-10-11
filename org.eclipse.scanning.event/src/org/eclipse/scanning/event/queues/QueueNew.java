package org.eclipse.scanning.event.queues;

import java.net.URI;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.queues.IQueueNew;
import org.eclipse.scanning.api.event.queues.QueueStatus;
import org.eclipse.scanning.api.event.queues.beans.Queueable;

public class QueueNew<T extends Queueable> implements IQueueNew<T> {

	private final String queueID;
	private final URI uri;
	private final IConsumer<T> consumer;

	private final String submissionQueueName, statusSetName, statusTopicName, 
	heartbeatTopicName, commandSetName, commandTopicName;

	private QueueStatus status;

	/**
	 * Constructs a Queue object from minimal arguments. Names of heartbeat 
	 * topic and commmand set/topic will be automatically generated, based on 
	 * the suffixes in {@link IQueueNew}.
	 * 
	 * @param queueID String name of queue.
	 * @param uri URI of the broker.
	 * @throws EventException When consumer cannot be created.
	 */
	public QueueNew(String queueID, URI uri) throws EventException {
		this(queueID, uri, queueID+IQueueNew.HEARTBEAT_TOPIC_SUFFIX);
	}

	/**
	 * Constructs a Queue with heartbeats published to a specific destination. 
	 * Command set/topics will be automatically generated, based on  the 
	 * suffixes in {@link IQueueNew}.
	 * 
	 * @param queueID String name of queue.
	 * @param uri URI of the broker
	 * @param heartbeatTopicName String topic name where heartbeats published.
	 * @throws EventException When consumer cannot be created.
	 */
	public QueueNew(String queueID, URI uri, String heartbeatTopicName) throws EventException {
		this(queueID, uri, heartbeatTopicName, queueID+IQueueNew.COMMAND_SET_SUFFIX, 
				queueID+IQueueNew.COMMAND_TOPIC_SUFFIX);
	}

	/**
	 * Constructs a Queue with heartbeats & commands published to specific 
	 * destinations.
	 *  
	 * @param queueID String name of queue.
	 * @param uri URI of the broker
	 * @param heartbeatTopicName String topic name where heartbeats published.
	 * @param commandSetName String queue name where consumer commands will be 
	 *                       stored.
	 * @param commandTopicName String topic name where commands will be 
	 *                         published.
	 * @throws EventException When consumer cannot be created.
	 */
	public QueueNew(String queueID, URI uri, String heartbeatTopicName,
			String commandSetName, String commandTopicName) throws EventException {
		this.queueID = queueID;
		this.uri = uri;

		//Record all the destination paths
		submissionQueueName = queueID+IQueueNew.SUBMISSION_QUEUE_SUFFIX;
		statusSetName = queueID+IQueueNew.STATUS_SET_SUFFIX;
		statusTopicName = queueID+IQueueNew.STATUS_TOPIC_SUFFIX; 
		this.heartbeatTopicName = heartbeatTopicName;
		this.commandSetName = commandSetName;
		this.commandTopicName = commandTopicName;

		IEventService eventService = QueueServicesHolder.getEventService();
		consumer = eventService.createConsumer(this.uri, getSubmissionQueueName(),
				getStatusSetName(), getStatusTopicName(), getHeartbeatTopicName(),
				getCommandTopicName());
		consumer.setRunner(new QueueProcessCreator<T>(true));

		status = QueueStatus.INITIALISED;
	}

	@Override
	public String getQueueID() {
		return queueID;
	}

	@Override
	public void start() throws EventException {
		consumer.start();
		status = QueueStatus.STARTED;
	}

	@Override
	public void stop() throws EventException {
		consumer.stop();
		status = QueueStatus.STOPPED;
	}

	@Override
	public void disconnect() throws EventException {
		consumer.disconnect();
		status = QueueStatus.DISPOSED;
	}

	@Override
	public IConsumer<T> getConsumer() {
		return consumer;
	}

	@Override
	public QueueStatus getStatus() {
		return status;
	}

	@Override
	public String getSubmissionQueueName() {
		return submissionQueueName;
	}

	@Override
	public String getStatusSetName() {
		return statusSetName;
	}

	@Override
	public String getStatusTopicName() {
		return statusTopicName;
	}

	@Override
	public String getHeartbeatTopicName() {
		return heartbeatTopicName;
	}

	@Override
	public String getCommandSetName() {
		return commandSetName;
	}

	@Override
	public String getCommandTopicName() {
		return commandTopicName;
	}

	@Override
	public URI getURI() {
		return uri;
	}

	@Override
	public boolean clearQueues() throws EventException {
		consumer.clearQueue(getSubmissionQueueName());
		consumer.clearQueue(getStatusSetName());

		if (consumer.getStatusSet().size() == 0 && consumer.getSubmissionQueue().size() == 0) return true;
		else return false;
	}

}
