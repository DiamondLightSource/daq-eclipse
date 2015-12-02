package org.eclipse.scanning.malcolm.core;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.scanning.api.malcolm.IMalcolmConnection;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnectorService;
import org.eclipse.scanning.api.malcolm.connector.MessageGenerator;
import org.eclipse.scanning.api.malcolm.message.JsonMessage;

public class MalcolmConnection implements IMalcolmConnection {

	private IMalcolmConnectorService<JsonMessage> connector;	
	private Map<String, IMalcolmDevice>    devices;
	private MalcolmService                 service;
	private URI                            malcolmUri;

	MalcolmConnection(MalcolmService service, URI malcolmUri) throws MalcolmDeviceException {
		this(service, malcolmUri, createConnectorService());
	}

	/**
	 * 
	 * @param connectorService - null in the OSGi services scenario
	 * @param malcolmUri - 
	 * @throws MalcolmDeviceException
	 */
	MalcolmConnection(MalcolmService service, URI malcolmUri, IMalcolmConnectorService<JsonMessage> connector) throws MalcolmDeviceException {
		
		this.service   = service;
		this.malcolmUri= malcolmUri;
		this.connector = connector; // Usually null unless we are in testing mode.
		this.devices   = new ConcurrentHashMap<String, IMalcolmDevice>(4);
		
		// At this point we get OSGi to get the connector service for us.
		try {
			if (connector==null) throw new MalcolmDeviceException("Cannot find an implementation of IConnectorService!");
			connector.connect(malcolmUri);
			
		} catch (Exception e) {
			throw new MalcolmDeviceException(null, "Cannot get connection services!", e);
		}
	}

	private static IMalcolmConnectorService<JsonMessage> createConnectorService() throws MalcolmDeviceException {
		
		try {
			Collection<IMalcolmConnectorService<JsonMessage>> services = MalcolmActivator.getConnectionServices();
			if (services.size()>1) throw new Exception("We have more than one connector service and not information to choose between them!"); // TODO
	
			return services.iterator().next();
		} catch (Exception ne) {
			throw new MalcolmDeviceException("Cannot find a service implementing IConnectorService!");
		}
	}

	@Override
	public Collection<String> getDeviceNames() throws MalcolmDeviceException {
		
		MessageGenerator<JsonMessage> connection = connector.createConnection();
		JsonMessage request = connection.createGetMessage("DirectoryService.attributes.instancesDevice.value");
		JsonMessage reply   = connector.send(null, request);
		final Collection<String> names = (Collection<String>)reply.getValue();
		return names;
	}

	@Override
	public IMalcolmDevice getDevice(String name) throws MalcolmDeviceException {
		
		if (devices.containsKey(name)) return devices.get(name);
		
		IMalcolmDevice device = new MalcolmDevice(name, connector); // Might throw exception
		devices.put(name, device);
		return device;
	}

	@Override
	public void dispose() throws MalcolmDeviceException {
		devices.clear();
		connector.disconnect();
	}

	IMalcolmConnectorService<JsonMessage> getConnector() {
		return connector;
	}
}
