package org.eclipse.scanning.test.event.queues.mocks;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.status.StatusBean;

public class MockConsumer<U extends StatusBean> implements IConsumer<U> {

	private UUID consumerId;
	
	public MockConsumer() {
		consumerId = UUID.randomUUID();
	}
	@Override
	public String getStatusSetName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStatusSetName(String queueName) throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getSubmitQueueName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSubmitQueueName(String queueName) throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<U> getQueue(String queueName, String fieldName) throws EventException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearQueue(String queueName) throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cleanQueue(String queueName) throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean reorder(U bean, String queueName, int amount) throws EventException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean remove(U bean, String queueName) throws EventException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean replace(U bean, String queueName) throws EventException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Class<U> getBeanClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBeanClass(Class<U> beanClass) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<U> getQueue() throws EventException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URI getUri() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IEventConnectorService getConnectorService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<U> getSubmissionQueue() throws EventException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<U> getStatusSet() throws EventException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStatusTopicName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStatusTopicName(String queueName) throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRunner(IProcessCreator<U> process) throws EventException {
		// TODO Auto-generated method stub
		
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
	public void run() throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IProcessCreator<U> getRunner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCommandTopicName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCommandTopicName(String commandTName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public UUID getConsumerId() {
		return consumerId;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disconnect() throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDurable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setDurable(boolean durable) {
		// TODO Auto-generated method stub
		
	}


}
	