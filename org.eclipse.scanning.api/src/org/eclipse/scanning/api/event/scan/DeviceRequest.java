package org.eclipse.scanning.api.event.scan;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.event.IdBean;

/**
 * 
 * Object used to query which devices are available from a given broker.
 * The solstice service for instance asks the IDeviceService what is available
 * and returns a list of devices and their configuration.
 * 
 * @author Matthew Gerring
 *
 */
public class DeviceRequest extends IdBean {

	private List<DeviceInformation> devices;

	public List<DeviceInformation> getDevices() {
		return devices;
	}

	public void setDevices(List<DeviceInformation> devices) {
		this.devices = devices;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((devices == null) ? 0 : devices.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeviceRequest other = (DeviceRequest) obj;
		if (devices == null) {
			if (other.devices != null)
				return false;
		} else if (!devices.equals(other.devices))
			return false;
		return true;
	}

	public void addDeviceInformation(DeviceInformation info) {
		if (devices==null) devices = new ArrayList<>(7);
		devices.add(info);
	}
}
