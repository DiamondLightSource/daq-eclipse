package org.eclipse.scanning.test.event.queues.mocks;

import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.beans.IAtomWithChildQueue;
import org.eclipse.scanning.api.event.status.StatusBean;

public class MockPublisher<T> implements IPublisher<T> {
	
	
	private String topicName;
	private final URI uri;
	private String queueName;
	private IConsumer<?> consumer;
	
	private List<DummyQueueable> broadcastBeans = new ArrayList<>();
	
	private boolean alive;
	
	public MockPublisher(URI uri, String topic) {
		//Removed from sig: IEventConnectorService service
		this.topicName = topic;
		this.uri = uri;
		
		alive = true;
	}

	@Override
	public String getTopicName() {
		return topicName;
	}

	@Override
	public void setTopicName(String topic) throws EventException {
		this.topicName = topic;
		
	}

	@Override
	public void disconnect() throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public URI getUri() {
		return uri;
	}
	
	@Override
	public void broadcast(T bean) throws EventException {
		final DummyQueueable broadBean = new DummyQueueable();
		broadBean.setMessage(((StatusBean)bean).getMessage());
		broadBean.setPreviousStatus(((StatusBean)bean).getPreviousStatus());
		broadBean.setStatus(((StatusBean)bean).getStatus());
		broadBean.setPercentComplete(((StatusBean)bean).getPercentComplete());
		broadBean.setUniqueId(((StatusBean)bean).getUniqueId());
		broadBean.setName(((StatusBean)bean).getName());
		
		if (bean instanceof IAtomWithChildQueue) {
			broadBean.setQueueMessage(((IAtomWithChildQueue)bean).getQueueMessage());
		}
		
		broadcastBeans.add(broadBean);
	}
	
	public List<DummyQueueable> getBroadcastBeans() {
		return broadcastBeans;
	}

	@Override
	public void setAlive(boolean alive) throws EventException {
		this.alive = alive;
	}

	@Override
	public boolean isAlive() {
		return alive;
	}

	@Override
	public void setStatusSetName(String queueName) {
		this.queueName = queueName;
		
	}

	@Override
	public String getStatusSetName() {
		return queueName;
	}
	
	@Override
	public void setStatusSetAddRequired(boolean required) {
		throw new RuntimeException("setStatusSetAddRequired is not implemented!");
	}


	@Override
	public void setLoggingStream(PrintStream stream) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IEventConnectorService getConnectorService() {
		// TODO Auto-generated method stub
		return null;
	}

	public IConsumer<?> getConsumer() {
		return consumer;
	}

	public void setConsumer(IConsumer<?> consumer) {
		this.consumer = consumer;
	}

}
