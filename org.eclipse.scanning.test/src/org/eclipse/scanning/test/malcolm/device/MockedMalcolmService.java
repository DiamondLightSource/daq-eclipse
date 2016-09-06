package org.eclipse.scanning.test.malcolm.device;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.eclipse.scanning.api.malcolm.IMalcolmConnection;
import org.eclipse.scanning.api.malcolm.IMalcolmService;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnectorService;
import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;

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
		if (connection==null) connection = new MockedMalcolmConnection(latcher, pausable);
		return connection;
	}

	public void dispose() throws MalcolmDeviceException {
		if (connection!=null) connection.dispose();
		connection = null;
	}

	@Override
	public IMalcolmConnection createConnection(URI malcolmUri, IMalcolmConnectorService<MalcolmMessage> connectorService) throws URISyntaxException, MalcolmDeviceException {
		throw new MalcolmDeviceException("Method createConnection(URI malcolmUri, IConnectorService<MalcolmMessage> connectorService) not implemented for "+getClass().getName());
	}
}
