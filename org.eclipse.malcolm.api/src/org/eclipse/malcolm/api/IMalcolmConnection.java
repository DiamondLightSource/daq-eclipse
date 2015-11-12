package org.eclipse.malcolm.api;

import java.util.Collection;

public interface IMalcolmConnection {

	/**
	 * List the names of the available devices
	 * @return
	 * @throws MalcolmDeviceException
	 */
	public Collection<String> getDeviceNames() throws MalcolmDeviceException;
	
	/**
	 * Get a device by name
	 * @param name
	 * @return
	 * @throws MalcolmDeviceException
	 */
	public IMalcolmDevice getDevice(String name) throws MalcolmDeviceException;

	/**
	 * Disposes the connections to all devices made by this connection.
	 */
	public void dispose() throws MalcolmDeviceException;
}
