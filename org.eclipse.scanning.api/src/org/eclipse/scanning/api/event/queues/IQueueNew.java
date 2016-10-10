package org.eclipse.scanning.api.event.queues;

import java.util.UUID;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.queues.beans.IQueueable;

public interface IQueueNew<T extends IQueueable> {
	
	/**
	 * Suffixes to be appended to the names of the destinations within a 
	 * concrete instance of IQueue
	 */
	public static final String SUBMISSION_QUEUE_SUFFIX = ".submission.queue";
	public static final String STATUS_SET_SUFFIX = ".status.queue";
	public static final String STATUS_TOPIC_SUFFIX = ".status.topic";
	public static final String HEARTBEAT_TOPIC_SUFFIX = ".heartbeat.topic";
	public static final String COMMAND_SET_SUFFIX = ".command.queue";
	public static final String COMMAND_TOPIC_SUFFIX = ".command.topic";
	
	/**
	 * Start the Queue consumer.
	 * 
	 * @throws EventException
	 */
	public void start() throws EventException;
	
	/**
	 * Stop the running consumer.
	 * 
	 * @throws EventException
	 */
	public void stop() throws EventException;
	
	/**
	 * Disconnect the consumer (and the heartbeat monitor).
	 * 
	 * @throws EventException if cannot access services.
	 */
	public void disconnect() throws EventException;
	
	/**
	 * Return unique consumer responsible for this queue. Consumer should 
	 * operate on objects (T) extending {@link IQueueable}.
	 * 
	 * @return IConsumer Queue consumer.
	 */
	public IConsumer<T> getConsumer();

	/**
	 * Return the unique UUID of this queue's consumer.
	 * 
	 * @return UUID unique to the queue consumer
	 */
	public default UUID getConsumerID() {
		return getConsumer().getConsumerId();
	}
	
	/**
	 * Report the current running state of the Queue.
	 * 
	 * @return QueueStatus
	 */
	public QueueStatus getStatus();
	
	/**
	 * Return the submission queue name.
	 * 
	 * @Return String submission queue name.
	 */
	public String getSubmissionQueueName();

	/**
	 * Return the status set name.
	 * 
	 * @return String status queue name.
	 */
	public String getStatusSetName();

	/**
	 * Return the status topic name.
	 * 
	 * @Return String submission queue name.
	 */
	public String getStatusTopicName();

	/**
	 * Return the topic name where this queue publishes heartbeats.
	 * 
	 * @return String heartbeat topic name.
	 */
	public String getHeartbeatTopicName();

	/**
	 * Return the name of the set where commands sent to this queue are
	 * sent.
	 * 
	 * @return String command queue name.
	 */
	public String getCommandSetName();

	/**
	 * Return the topic name where commands can be passed to this queue.
	 * 
	 * @return String command topic name.
	 */
	public String getCommandTopicName();
	
	/**
	 * Clear both the submission and the status queues of any pending jobs
	 * 
	 * @throws EventException if cannot access consumer.
	 */
	public boolean clearQueues() throws EventException;

}
