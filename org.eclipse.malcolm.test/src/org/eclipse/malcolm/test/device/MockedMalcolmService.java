package org.eclipse.malcolm.test.device;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.eclipse.malcolm.api.IMalcolmConnection;
import org.eclipse.malcolm.api.IMalcolmService;
import org.eclipse.malcolm.api.MalcolmDeviceException;
import org.eclipse.malcolm.api.connector.IMalcolmConnectorService;
import org.eclipse.malcolm.api.message.JsonMessage;

public class MockedMalcolmService implements IMalcolmService {

	private IMalcolmConnection connection;
    private LatchDelegate      latcher;

	public MockedMalcolmService() {
		super();
		this.latcher = new LatchDelegate();
	}

	@Override
	public IMalcolmConnection createConnection(URI uri) throws URISyntaxException, MalcolmDeviceException {
		boolean pausable = uri != null && uri.getHost().equalsIgnoreCase("pausable");
		if (connection==null) connection = new MockedConnection(latcher, pausable);
		return connection;
	}

	public void dispose() throws MalcolmDeviceException {
		if (connection!=null) connection.dispose();
		connection = null;
	}

	@Override
	public IMalcolmConnection createConnection(URI malcolmUri, IMalcolmConnectorService<JsonMessage> connectorService) throws URISyntaxException, MalcolmDeviceException {
		throw new MalcolmDeviceException("Method createConnection(URI malcolmUri, IConnectorService<JsonMessage> connectorService) not implemented for "+getClass().getName());
	}
}
