package org.eclipse.scanning.api.event.core;

import java.net.URI;

import org.eclipse.scanning.api.event.IEventConnectorService;

public interface IURIConnection extends IDisconnectable{

	/**
	 * The URI of this connection.
	 * @return
	 */
	public URI getUri();
	
	/**
	 * The underlyng service which the uri is connected using
	 */
	public IEventConnectorService getConnectorService();
}
