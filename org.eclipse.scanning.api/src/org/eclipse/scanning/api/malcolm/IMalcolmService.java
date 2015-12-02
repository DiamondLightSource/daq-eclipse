package org.eclipse.scanning.api.malcolm;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnectorService;
import org.eclipse.scanning.api.malcolm.message.JsonMessage;

/**
 * An OSGi service for creating Malcolm connections
 * 
 * Usage:
 * <code>
 <p>
        IMalcolmService service = ... // OSGi service <br>
        IMalcolmConnection        connection = service.createConnection("tcp://127.0.0.1:7800");<br>
<br>
		IMalcolmDevice zebra =  connection.getDevice("zebra");<br>
	    Map<String, Object> config = new HashMap<String,Object>(2);<br>
		config.put("PC_BIT_CAP", 1);<br>
		config.put("PC_TSPRE", "ms");<br>
		<br>
		zebra.configure(config);<br>
		zebra.run(); // blocks until finished<br>
		<br>
		final State state = zebra.getState();<br>
        // ... We did something!<br>
</p>        
</code>

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
