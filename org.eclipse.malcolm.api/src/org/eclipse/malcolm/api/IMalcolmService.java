package org.eclipse.malcolm.api;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.malcolm.api.connector.IMalcolmConnectorService;
import org.eclipse.malcolm.api.message.JsonMessage;

/**
 * An OSGi service for creating Malcolm connections
 * 
 * @author Matthew Gerring
 *
 */
public interface IMalcolmService {

	/**
	 * Method to create a direct connection to a malcolm instance. Used by the server to 
	 * connect to Malcolm and do scans.
	 * 
	 * Finds the connector by looking for an OSGi service which implements IConnectorService
	 * 
	 * @param malcolmUri 
	 * @return the connection, should not return null, an exception will be thrown on error
	 * @throws URISyntaxException - if invalid URL
	 * @throws MalcolmDeviceException - if the connection is not ready or another error occurs connection
	 */
	public IMalcolmConnection createConnection(URI malcolmUri) throws URISyntaxException, MalcolmDeviceException;
	
	/**
	 * Convenience method used mostly by testing when OSGi is not available.
	 * The connectorService must be used only once for each connection URI.
	 * 
	 * @param malcolmUri
	 * @param connectorService - used to override the connector, useful for tests.
	 * @return
	 * @throws URISyntaxException
	 * @throws MalcolmDeviceException
	 */
	public IMalcolmConnection createConnection(URI malcolmUri, IMalcolmConnectorService<JsonMessage> connectorService) throws URISyntaxException, MalcolmDeviceException;
		
}
