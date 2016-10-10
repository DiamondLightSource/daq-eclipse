package org.eclipse.scanning.test.event.queues.mocks;

import java.net.URI;
import java.util.EventListener;
import java.util.List;

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

	@Override
	public boolean isDisconnected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addProperty(String name, FilterAction... action) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeProperty(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeListeners(String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSynchronous(boolean sync) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isSynchronous() {
		// TODO Auto-generated method stub
		return false;
	}

}
