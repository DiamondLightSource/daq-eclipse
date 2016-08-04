package org.eclipse.scanning.test.event.queues.mocks;

import java.net.URI;
import java.util.EventListener;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.core.ISubscriber;

public class MockSubscriber<T extends EventListener> implements ISubscriber<T> {
	
	public MockSubscriber(URI uri, String topicName) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getTopicName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTopicName(String topic) throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disconnect() throws EventException {
		// TODO Auto-generated method stub
		
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
	public void addListener(T listener) throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeListener(T listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListener(String id, T listener) throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeListener(String id, T listener) {
		// TODO Auto-generated method stub
		
	}

}
