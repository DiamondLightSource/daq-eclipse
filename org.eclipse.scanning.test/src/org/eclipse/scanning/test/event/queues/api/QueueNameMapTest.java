package org.eclipse.scanning.test.event.queues.api;

import static org.junit.Assert.assertEquals;

import org.eclipse.scanning.api.event.queues.QueueNameMap;
import org.junit.Test;

/**
 * Test functionality of the QueueNameMap class.
 * 
 * @author Michael Wharmby
 *
 */
public class QueueNameMapTest {
	
	private QueueNameMap qnm;
	
	@Test
	public void testCorrectNamesForQueues() {
		qnm = new QueueNameMap("submit.queue",
				"status.queue", "status.topic", 
				"heartbeat.topic", "kill.topic");
		
		assertEquals("submit.queue", qnm.get("submitQ"));
		assertEquals("status.queue", qnm.get("statusQ"));
		assertEquals("status.topic", qnm.get("statusT"));
		assertEquals("heartbeat.topic", qnm.get("heartbeatT"));
		assertEquals("kill.topic", qnm.get("killT"));
		
		assertEquals("submit.queue", qnm.getSubmissionQueueName());
		assertEquals("status.queue", qnm.getStatusQueueName());
		assertEquals("status.topic", qnm.getStatusTopicName());
		assertEquals("heartbeat.topic", qnm.getHeartbeatTopicName());
		assertEquals("kill.topic", qnm.getCommandTopicName());
	}
	
	@Test
	public void testAutomaticQueueNaming() {
		qnm = new QueueNameMap("uk.ac.diamond.i15-1");
		
		assertEquals("uk.ac.diamond.i15-1.submission.queue", qnm.getSubmissionQueueName());
		assertEquals("uk.ac.diamond.i15-1.status.queue", qnm.getStatusQueueName());
		assertEquals("uk.ac.diamond.i15-1.status.topic", qnm.getStatusTopicName());
		assertEquals("uk.ac.diamond.i15-1.heartbeat.topic", qnm.getHeartbeatTopicName());
		assertEquals("uk.ac.diamond.i15-1.kill.topic", qnm.getCommandTopicName());
	}
	
	@Test
	public void testAutomaticQueueNamingWithHeartBeat() {
		qnm = new QueueNameMap("uk.ac.diamond.i15-2", "uk.ac.diamond.i15-1.heartbeat.topic");
		
		assertEquals("uk.ac.diamond.i15-2.submission.queue", qnm.getSubmissionQueueName());
		assertEquals("uk.ac.diamond.i15-2.status.queue", qnm.getStatusQueueName());
		assertEquals("uk.ac.diamond.i15-2.status.topic", qnm.getStatusTopicName());
		assertEquals("uk.ac.diamond.i15-1.heartbeat.topic", qnm.getHeartbeatTopicName());
		assertEquals("uk.ac.diamond.i15-2.kill.topic", qnm.getCommandTopicName());
	}
	
	@Test
	public void testAutomaticQueueNamingWithHeartBeatKill() {
		qnm = new QueueNameMap("uk.ac.diamond.i15-2", "uk.ac.diamond.i15-1.heartbeat.topic", "uk.ac.diamond.i15-0.kill.topic");
		
		assertEquals("uk.ac.diamond.i15-2.submission.queue", qnm.getSubmissionQueueName());
		assertEquals("uk.ac.diamond.i15-2.status.queue", qnm.getStatusQueueName());
		assertEquals("uk.ac.diamond.i15-2.status.topic", qnm.getStatusTopicName());
		assertEquals("uk.ac.diamond.i15-1.heartbeat.topic", qnm.getHeartbeatTopicName());
		assertEquals("uk.ac.diamond.i15-0.kill.topic", qnm.getCommandTopicName());
	}

}
