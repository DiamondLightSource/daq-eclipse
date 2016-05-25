package org.eclipse.scanning.api.event.queues;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.alive.HeartbeatBean;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.queues.beans.IQueueable;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.StatusBean;

/**
 * IQueue object contain all the information necessary to interact with and 
 * control a queue within the IQueue service. IQueue objects contain an
 * {@link IConsumer}, which runs the queue, and all of it's configuration, plus
 *  information on whether the queue is currently alive and its 
 *  {@link QueueStatus}.
 * 
 * @author Michael Wharmby
 *
 * @param <T> bean object extending {@link Queueable} which is passed around 
 *            this queue. 
 */
public interface IQueue<T extends Queueable> {
	
	/**
	 * Get the unique ID of this queue (should be same as that stored in 
	 * {@link IQueueService} registry).
	 * 
	 * @return String Unique name of queue in registry.
	 */
	public String getQueueID();
	
	/**
	 * Return the current operational state of this queue.
	 * 
	 * @return {@link QueueStatus} describing state of queue.
	 */
	public QueueStatus getQueueStatus();
	
	/**
	 * Changes the current operational state of this queue.
	 * 
	 * @param new {@link QueueStatus} of this queue 
	 */
	public void setQueueStatus(QueueStatus status);
	
	/**
	 * The unique consumer responsible for this queue. Consumer should operate 
	 * on objects (T) extending both {@link StatusBean} and {@link IQueueable}.
	 * 
	 * @return IConsumer Queue consumer.
	 */
	public IConsumer<T> getConsumer();
	
	/**
	 * Return the unique UUID of this queue's consumer.
	 * 
	 * @return UUID unique to the queue consumer
	 */
	public UUID getConsumerID();
	
	/**
	 * Return the submission queue name.
	 * 
	 * @Return String submission queue name.
	 */
	public String getSubmissionQueueName();
	
	/**
	 * Return the status queue name.
	 * 
	 * @return String status queue name.
	 */
	public String getStatusQueueName();
	
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
	 * Return the topic name where commands can be passed to this queue.
	 * 
	 * @return String command topic name.
	 */
	public String getCommandTopicName();
	
	/**
	 * Returns a class containing the queue/topic names configured for this 
	 * queue.
	 * 
	 * @return {@link QueueNameMap} containing the configured queue names for 
	 * 		   this queue.
	 */
	public Map<String, String> getQueueNames();
	
	/**
	 * Return the {@link IProcessCreator} currently set for use on the consumer.
	 * 
	 * @return {@link IProcessCreator} currently set on the consumer.
	 */
	public IProcessCreator<T> getProcessor();
	
	/**
	 * Change the {@link IProcessCreator} which the consumer will use to 
	 * process queue beans.
	 * 
	 * @param processor Instance of {@link IProcessCreator} to use.
	 * @throws EventException If the consumer rejects the offered processor
	 */
	public void setProcessor(IProcessCreator<T> processor) throws EventException;
	
	/**
	 * Return a list of the most recent {@link HeartbeatBean}s heard by 
	 * this queue.
	 * 
	 * @return Size limited queue of the most recent heartbeats. 
	 */
	public List<HeartbeatBean> getLatestHeartbeats();
	
	/**
	 * Return the most recent {@link HeartbeatBean} heard by the queue.
	 * 
	 * @return The last heartbeat observed.
	 */
	public HeartbeatBean getLastHeartbeat();
	
	/**
	 * Clear both the submission and the status queues of any pending jobs
	 * 
	 * @throws EventException if cannot access consumer.
	 */
	public boolean clearQueues() throws EventException;
	
	/**
	 * Disconnect the heartbeat monitor and the consumer.
	 * 
	 * @throws EventException if cannot access services.
	 */
	public void disconnect() throws EventException;

	/**
	 * Determines whether there are jobs pending on the submission queue.
	 * 
	 * @return true if there are jobs to process still.
	 * @throws EventException if cannot access consumer.
	 */
	public boolean hasSubmittedJobsPending() throws EventException;

}
