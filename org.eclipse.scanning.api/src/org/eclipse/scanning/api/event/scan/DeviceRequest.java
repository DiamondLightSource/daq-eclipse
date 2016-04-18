package org.eclipse.scanning.api.event.scan;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.eclipse.scanning.api.event.IdBean;

/**
 * 
 * Object used to query which devices are available from a given broker.
 * The solstice service for instance asks the IDeviceService what is available
 * and returns a list of devices and their configuration.
 * 
 * Servlet may also be used to configure a given device and return the result.
 * 
 * @author Matthew Gerring
 *
 */
public class DeviceRequest extends IdBean {
	
	/**
	 * List of all devices
	 */
	private Collection<DeviceInformation<?>> devices;

	/**
	 * A regular expression or string which if set only
	 * returns the devices matching this name.
	 */
	private String deviceName;
	
	/**
	 * The device's model. Normally used to configure a device.
	 */
	private Object deviceModel;
	
	public Collection<DeviceInformation<?>> getDevices() {
		return devices;
	}

	public void setDevices(Collection<DeviceInformation<?>> devices) {
		this.devices = devices;
	}
	
	@Override
	public <A extends IdBean> void merge(A with) {
		super.merge(with);
		DeviceRequest dr = (DeviceRequest)with;
		if (devices==null) devices = dr.devices;
		if (devices!=null) devices.addAll(dr.devices);
		deviceName    = dr.deviceName;
		deviceModel   = dr.deviceModel;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((deviceModel == null) ? 0 : deviceModel.hashCode());
		result = prime * result + ((deviceName == null) ? 0 : deviceName.hashCode());
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
		if (deviceModel == null) {
			if (other.deviceModel != null)
				return false;
		} else if (!deviceModel.equals(other.deviceModel))
			return false;
		if (deviceName == null) {
			if (other.deviceName != null)
				return false;
		} else if (!deviceName.equals(other.deviceName))
			return false;
		if (devices == null) {
			if (other.devices != null)
				return false;
		} else if (!devices.equals(other.devices))
			return false;
		return true;
	}

	public void addDeviceInformation(DeviceInformation<?> info) {
		if (devices==null) devices = new LinkedHashSet<DeviceInformation<?>>(7);
		devices.add(info);
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String dn) {
		this.deviceName = dn;
	}

	@SuppressWarnings("unchecked")
	public Object getDeviceModel() {
		return deviceModel;
	}

	public void setDeviceModel(Object dm) {
		this.deviceModel = dm;
	}

	public boolean isEmpty() {
		return devices==null ? true : devices.isEmpty();
	}

	public DeviceInformation<?> getDeviceInformation() {
		return (devices==null) ? null : devices.iterator().next();
	}
}
