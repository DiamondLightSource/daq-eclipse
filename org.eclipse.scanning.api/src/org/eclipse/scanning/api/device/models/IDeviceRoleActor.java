package org.eclipse.scanning.api.device.models;

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
	 * @return
	 */
	DeviceRole getRole();
	
	/**
	 * The role of the device.
	 * @param role
	 */
	void setRole(DeviceRole role);
}
