package org.eclipse.scanning.api.malcolm;

import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.event.scan.DeviceState;


public interface ILatchableDevice {

	/**
	 * Instruct the device to return once a given state is reached.
	 * @param time or less than 0 to wait infinitely (not recommended)
	 * @param unit
	 * @param ignoredStates
	 * @throws MalcolmDeviceException
	 */
	public DeviceState latch(long time, TimeUnit unit, DeviceState... ignoredStates) throws MalcolmDeviceException; 
}
