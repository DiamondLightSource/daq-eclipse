package org.eclipse.scanning.api.event.queues;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scanning.api.event.alive.HeartbeatBean;
import org.eclipse.scanning.api.event.status.Status;

/**
 * Class to store the names of the queues/topics configured for an 
 * {@link IQueue} object. The map contains the following keys which map to the
 * queue/topic names:
 * 		submitQ - submission queue
 * 		statusQ - status queue
 * 		statusT - status topic
 * 		heartbeatT - heartbeat topic
 * 		killT - kill topic
 * 
 * These can be used with the get method or directly on the map, returned by 
 * getMap(). Each name also has a specific getter method associated with it.
 * 
 * If given only a base name for the queue root, the class auto-generates the
 * names of the queue/topics.
 * 
 * @author Michael Wharmby
 *
 */
public class QueueNameMap {
	
	public static final String SUBMISSION_QUEUE = "submitQ";
	public static final String SUBMISSION_QUEUE_SUFFIX = ".submission.queue";
	public static final String STATUS_QUEUE = "statusQ";
	public static final String STATUS_QUEUE_SUFFIX = ".status.queue";
	public static final String STATUS_TOPIC = "statusT";
	public static final String STATUS_TOPIC_SUFFIX = ".status.topic";
	public static final String HEARTBEAT_TOPIC = "heartbeatT";
	public static final String HEARTBEAT_TOPIC_SUFFIX = ".heartbeat.topic";
	public static final String CMD_TOPIC = "killT";
	public static final String CMD_TOPIC_SUFFIX = ".kill.topic";
	
	
	private final Map<String, String> nameMap;
	
	/**
	 * Create new QueueNameMap with fully auto-generated names of queues/topics.
	 * 
	 * @param qRoot String base name for name/topic auto-generation.
	 */
	public QueueNameMap(String qRoot) {
		nameMap = new HashMap<String, String>();
		nameMap.put(SUBMISSION_QUEUE, qRoot + SUBMISSION_QUEUE_SUFFIX);
		nameMap.put(STATUS_QUEUE, qRoot + STATUS_QUEUE_SUFFIX);
		nameMap.put(STATUS_TOPIC, qRoot + STATUS_TOPIC_SUFFIX);
		nameMap.put(HEARTBEAT_TOPIC, qRoot + HEARTBEAT_TOPIC_SUFFIX);
		nameMap.put(CMD_TOPIC, qRoot + CMD_TOPIC_SUFFIX);
	}
	
	/**
	 * Create new QueueNameMap with auto-generated names of queues/topics, but 
	 * a pre-defined HeartbeatTopic.
	 * 
	 * @param qRoot String base name for name/topic auto-generation.
	 * @param heartT String topic for heartbeat.
	 */
	public QueueNameMap(String qRoot, String heartT) {
		nameMap = new HashMap<String, String>();
		nameMap.put(SUBMISSION_QUEUE, qRoot + SUBMISSION_QUEUE_SUFFIX);
		nameMap.put(STATUS_QUEUE, qRoot + STATUS_QUEUE_SUFFIX);
		nameMap.put(STATUS_TOPIC, qRoot + STATUS_TOPIC_SUFFIX);
		nameMap.put(HEARTBEAT_TOPIC, heartT);
		nameMap.put(CMD_TOPIC, qRoot + CMD_TOPIC_SUFFIX);

	}
	
	/**
	 * Create new QueueNameMap with auto-generated names of queues/topics, but 
	 * with pre-defined HeartbeatTopic and CommantTopics.
	 * 
	 * @param qRoot String base name for name/topic auto-generation.
	 * @param heartT String topic for heartbeat.
	 * @param cmdT String topic for commands.
	 */
	public QueueNameMap(String qRoot, String heartT, String cmdT) {
		nameMap = new HashMap<String, String>();
		nameMap.put(SUBMISSION_QUEUE, qRoot + SUBMISSION_QUEUE_SUFFIX);
		nameMap.put(STATUS_QUEUE, qRoot + STATUS_QUEUE_SUFFIX);
		nameMap.put(STATUS_TOPIC, qRoot + STATUS_TOPIC_SUFFIX);
		nameMap.put(HEARTBEAT_TOPIC, heartT);
		nameMap.put(CMD_TOPIC, cmdT);

	}
	
	/**
	 * Create a new QueueNameMap with names of each queue/topic specified.
	 * 
	 * @param submQ String submission queue name
	 * @param statQ String status queue name
	 * @param statT String status topic name
	 * @param heartT String heartbeat topic name
	 * @param cmdT String command topic name
	 */
	public QueueNameMap(String submQ, String statQ, String statT,
			String heartT, String cmdT) {
		nameMap = new HashMap<String, String>();
		nameMap.put(SUBMISSION_QUEUE, submQ);
		nameMap.put(STATUS_QUEUE, statQ);
		nameMap.put(STATUS_TOPIC, statT);
		nameMap.put(HEARTBEAT_TOPIC, heartT);
		nameMap.put(CMD_TOPIC, cmdT);
	}
	
	/**
	 * Return the value of a given queue/topic
	 * 
	 * @param key String constant name of queue/topic
	 * @return String name of queue/topic
	 */
	public String get(String key) {
		return nameMap.get(key);
	}
	
	/**
	 * Return the complete map of all queues & topics with their string 
	 * constant keys
	 * 
	 * @return Map<String, String> of queue & topic names.
	 */
	public Map<String, String> getMap() {
		return nameMap;
	}
	
	/**
	 * Return the name of the submission queue for this consumer, where beans 
	 * are submitted to for processing.
	 * 
	 * @return String name of the submission queue.
	 */
	public String getSubmissionQueueName() {
		return nameMap.get(SUBMISSION_QUEUE);
	}
	
	/**
	 * Return the name of the status queue for this consumer.
	 * 
	 * @return String name of the status queue.
	 */
	public String getStatusQueueName() {
		return nameMap.get(STATUS_QUEUE);
	}
	
	/**
	 * Return the name of the status topic for this consumer, where the 
	 * {@link Status} of beans in the queue is reported.
	 * 
	 * @return String name of the status topic.
	 */
	public String getStatusTopicName() {
		return nameMap.get(STATUS_TOPIC);
	}
	
	/**
	 * Return the name of the heartbeat topic for this consumer, where consumer
	 * {@link HeartbeatBean}s are published.
	 * 
	 * @return String name of the status topic.
	 */
	public String getHeartbeatTopicName() {
		return nameMap.get(HEARTBEAT_TOPIC);
	}
	
	/**
	 * Return the name of the command topic for this consumer, where commands
	 * should be published to control the consumer
	 * 
	 * @return String name of the command topic.
	 */
	public String getCommandTopicName() {
		return nameMap.get(CMD_TOPIC);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nameMap == null) ? 0 : nameMap.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueueNameMap other = (QueueNameMap) obj;
		if (nameMap == null) {
			if (other.nameMap != null)
				return false;
		} else if (!nameMap.equals(other.nameMap))
			return false;
		return true;
	}

}
