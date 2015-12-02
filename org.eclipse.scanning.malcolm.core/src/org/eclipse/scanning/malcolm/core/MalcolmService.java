package org.eclipse.scanning.malcolm.core;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.scanning.api.malcolm.IMalcolmConnection;
import org.eclipse.scanning.api.malcolm.IMalcolmService;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnectorService;
import org.eclipse.scanning.api.malcolm.message.JsonMessage;

public class MalcolmService implements IMalcolmService {
		
	/**
	 * Used by OSGi to make the service.
	 */
	public MalcolmService() {
	}

	@Override
	public IMalcolmConnection createConnection(URI malcolmUri) throws URISyntaxException, MalcolmDeviceException {
		return new MalcolmConnection(this, malcolmUri);
	}


	@Override
	public IMalcolmConnection createConnection(URI malcolmUri, IMalcolmConnectorService<JsonMessage> connectorService) throws URISyntaxException, MalcolmDeviceException {
		return new MalcolmConnection(this, malcolmUri, connectorService);
	}
}
