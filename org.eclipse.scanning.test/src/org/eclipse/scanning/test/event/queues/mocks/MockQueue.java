package org.eclipse.scanning.test.event.queues.mocks;

import java.util.List;
import java.util.UUID;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.alive.HeartbeatBean;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.queues.IQueue;
import org.eclipse.scanning.api.event.queues.QueueNameMap;
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
	public QueueStatus getQueueStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setQueueStatus(QueueStatus status) {
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
	public QueueNameMap getQueueNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IProcessCreator<T> getProcessor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setProcessor(IProcessCreator<T> processor) throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<HeartbeatBean> getLatestHeartbeats() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HeartbeatBean getLastHeartbeat() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean clearQueues() throws EventException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void disconnect() throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasSubmittedJobsPending() throws EventException {
		// TODO Auto-generated method stub
		return false;
	}

}
