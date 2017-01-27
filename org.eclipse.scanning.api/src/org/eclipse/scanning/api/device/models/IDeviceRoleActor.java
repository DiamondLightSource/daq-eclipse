package org.eclipse.scanning.api.device.models;

import java.util.EnumSet;
import java.util.Set;

/**
 * A device which has a clear role in the system.
 * {@link DeviceRole}
 * 
 * @author Matthew Gerring
 *
 */
public interface IDeviceRoleActor {

	/**
	 * The role of the device.
	 * @return role of the device
	 */
	DeviceRole getRole();
	
	/**
	 * The role of the device.
	 * @param role role of the device
	 */
	void setRole(DeviceRole role);
	
	/**
	 * Returns a set of the scan modes that this device can participate in.
	 * @return possible roles of the device
	 */
	Set<ScanMode> getSupportedScanModes();
	
}
