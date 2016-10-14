package org.eclipse.scanning.test.event.queues.mocks;

import java.net.URI;
import java.util.UUID;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.queues.IQueue;
import org.eclipse.scanning.api.event.queues.QueueStatus;
import org.eclipse.scanning.api.event.queues.beans.Queueable;

public class MockQueue<T extends Queueable> implements IQueue<T> {
	
	private String queueID;
	private IConsumer<T> cons;
	
	public MockQueue(String queueID, IConsumer<T> cons) {
		this.queueID = queueID;
		this.cons = cons;
	}

	@Override
	public String getQueueID() {
		return queueID;
	}

	@Override
	public QueueStatus getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStatus(QueueStatus status) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IConsumer<T> getConsumer() {
		return cons;
	}

	@Override
	public UUID getConsumerID() {
		return cons.getConsumerId();
	}

	@Override
	public boolean clearQueues() throws EventException {
		cons.clearQueue(cons.getStatusSetName());
		cons.clearQueue(cons.getSubmitQueueName());
		return true;
	}

	@Override
	public void disconnect() throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getSubmissionQueueName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStatusTopicName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHeartbeatTopicName() {
		// TODO Auto-generated method stub
		return IEventService.HEARTBEAT_TOPIC;
	}

	@Override
	public String getCommandTopicName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void start() throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getStatusSetName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCommandSetName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URI getURI() {
		// TODO Auto-generated method stub
		return null;
	}

}
