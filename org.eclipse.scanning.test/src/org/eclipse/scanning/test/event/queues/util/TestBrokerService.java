package org.eclipse.scanning.test.event.queues.util;

import java.net.URI;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.test.BrokerTest;

public class TestBrokerService extends BrokerTest {
	
	private boolean active = false;
	
	public void start() throws Exception {
		startBroker();
		active = true;
	}
	
	public void stop() throws Exception {
		stopBroker();
		active = false;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public URI getURI() throws EventException {
		if (isActive()) return uri;
		else throw new EventException("Test Broker Service not started!");
	}

}
