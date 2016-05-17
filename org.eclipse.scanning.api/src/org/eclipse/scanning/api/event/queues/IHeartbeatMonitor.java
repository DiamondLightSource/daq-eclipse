package org.eclipse.scanning.api.event.queues;

import java.util.List;
import java.util.UUID;

import org.eclipse.scanning.api.event.alive.HeartbeatBean;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.queues.beans.Queueable;

/**
 * A class composed of an {@link ISubscriber} configured to listen for
 * {@link HeartbeatBean}s and then record them into a 
 * {@link SizeLimitedRecorder}. 
 * 
 * The topic on which this monitor listens can be configured, as can the 
 * consumer ID which is listened for. The consumer ID can be set either 
 * directly using the UUID of the consumer (in the most general case), using
 * an instance of an {@link IQueue} object or using the String ID of an 
 * {@link IQueue} object in combination with its associated 
 * {@link IQueueService}.
 * 
 * @author Michael Wharmby
 *
 */
public interface IHeartbeatMonitor {
	
	/**
	 * Get the most recent {@link HeartbeatBean} from the record.
	 * 
	 * @return Last recorded {@link HeartbeatBean}.
	 */
	public HeartbeatBean getLastHeartbeat();
	
	/**
	 * Get the complete set or recorded {@link HeartbeatBean}s.
	 * 
	 * @return List of recorded {@link HeartbeatBean}.
	 */
	public List<HeartbeatBean> getLatestHeartbeats();
	
	/**
	 * Return the topic on which this monitor will listen for beats.
	 * 
	 * @return String name of topic this monitor listens on.
	 */
	public String getHeartbeatTopic();
	
	/**
	 * Change the topic on which this monitor listens for 
	 * {@link HeartbeatBean}s.
	 * 
	 * @param topicName String name of topic to listen on.
	 */
	public void setHeartbeatTopic(String topicName);

	/**
	 * Return the unique ID of the consumer being monitored.
	 * 
	 * @return UUID of the consumer being monitored.
	 */
	public UUID getConsumerID();
	
	/**
	 * Change the unique ID of the consumer to be monitored.
	 * 
	 * @param consumerID - UUID of consumer to be monitored.
	 */
	public void setConsumerID(UUID consumerID);
	
	/**
	 * Change the {@link IQueue} object which will be monitored. This also
	 * changes the consumer ID under the hood.
	 * 
	 * @param queue {@link IQueue} object to be monitored.
	 */
	public default void setQueue(IQueue<? extends Queueable> queue) {
		setConsumerID(queue.getConsumerID());
		setQueueID(queue.getQueueID());
	}
	
	/**
	 * Return string name of the queue containing a consumer being monitored.
	 * 
	 * @return String name of Queue object being monitored.
	 */
	public String getQueueID();

	/**
	 * Change the string name of the {@link IQueue} which is being monitored. 
	 * This does not actually change the queue being monitored.
	 * 
	 * @param queueID String name of {@link IQueue} object to be monitored.
	 */
	public void setQueueID(String queueID);
	
	/**
	 * Change the {@link IConsumer} being monitored by this object based on 
	 * the String name of the {@link IQueue} object and its associated 
	 * {@link IQueueService}. 
	 * 
	 * @param queueID String name of {@link IQueue} object to be monitored.
	 * @param queueService IQueueService responsible for {@link IQueue}.
	 */
	public default void setQueueID(String queueID, IQueueService queueService) {
		IQueue<? extends Queueable> queue;
		if (queueID.equals(queueService.getJobQueueID())) {
			queue = queueService.getJobQueue();
		} else {
			queue = queueService.getActiveQueue(queueID);
		}
		setQueue(queue);
	}
	
	/**
	 * Return the maximum number of beats which will be recorded.
	 * 
	 * @return Int max number of beats to be recorded.
	 */
	public int getRecorderSize();
	
	/**
	 * Change the maximum number of beats which can be recorded.
	 * 
	 * @param beats int number of beats to be recorded.
	 */
	public void setRecorderSize(int beats);

}
