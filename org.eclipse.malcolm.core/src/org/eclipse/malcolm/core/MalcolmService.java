package org.eclipse.malcolm.core;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.malcolm.api.IMalcolmConnection;
import org.eclipse.malcolm.api.IMalcolmService;
import org.eclipse.malcolm.api.MalcolmDeviceException;
import org.eclipse.malcolm.api.connector.IMalcolmConnectorService;
import org.eclipse.malcolm.api.message.JsonMessage;

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
