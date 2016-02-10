package org.eclipse.scanning.api.malcolm;

import java.util.Collection;

import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.ScanBean;

public interface IMalcolmConnection {

	/**
	 * List the names of the available devices
	 * @return
	 * @throws MalcolmDeviceException
	 */
	public Collection<String> getDeviceNames() throws MalcolmDeviceException;
	
	/**
	 * Get a device by name. At the point where the device is retrieved the
	 * caller may know the type of device and use a generic to declare its model.
	 * 
	 * @param name
	 * @return
	 * @throws MalcolmDeviceException
	 */
	public <T> IMalcolmDevice<T> getDevice(String name) throws MalcolmDeviceException;

	/**
	 * Get a device by name. At the point where the device is retrieved the
	 * caller may know the type of device and use a generic to declare it.
	 * 
	 * @param name
	 * @return
	 * @throws MalcolmDeviceException
	 */
	public <T> IMalcolmDevice<T> getDevice(String name, IPublisher<ScanBean> publisher) throws MalcolmDeviceException;

	/**
	 * Disposes the connections to all devices made by this connection.
	 */
	public void dispose() throws MalcolmDeviceException;
}
