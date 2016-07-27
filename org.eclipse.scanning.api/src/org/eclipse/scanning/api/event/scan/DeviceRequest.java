package org.eclipse.scanning.api.event.scan;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.eclipse.scanning.api.annotation.ui.DeviceType;
import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.points.IPosition;

/**
 * 
 * Object used to query which devices are available from a given broker.
 * The solstice service for instance asks the IDeviceService what is available
 * and returns a list of devices and their configuration.
 * 
 * Servlet may also be used to configure a given device and return the result.
 * 
 * <pre>
 * Usage:
 * 1. Set nothing, post returns list of DeviceInformation for all devices. 
 * 2. Set the device name, post returns the device with this name.               IRunnableDeviceService.getRunnableDevice()
 * 3. Set name and model, named device is retrieved and configured.        
 * 4. Set the device model and the configure boolean, get a new device created.  IRunnableDeviceService.createRunnableDevice()
 * 5. Set the device action and the device name to call specific methods.
 * 
 * </pre>
 * 
 * @author Matthew Gerring
 *
 */
public class DeviceRequest extends IdBean {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3680277076626446185L;

	private DeviceType deviceType = DeviceType.RUNNABLE;
	
	/**
	 * List of all devices
	 */
	private Collection<DeviceInformation<?>> devices;

	/**
	 * The name of the device required or null if more than one device is required.
	 */
	private String deviceName;
	
	/**
	 * The device's model. Normally used to configure a device.
	 * The Object must json through the marshaller.
	 */
	private Object deviceModel;
	
	/**
	 * Set wether a create call (one where the model is non-null)
	 * should call configure on the device.
	 */
	private boolean configure = true;
	
	/**
	 * The action to call if a method should be called on the device.
	 */
	private DeviceAction deviceAction;
	
	/**
	 * The position to start at for a run call.
	 */
	private IPosition position;
	
	/**
	 * If there is an error in the request.
	 */
	private String errorMessage;
	
	@Override
	public <A extends IdBean> void merge(A with) {
		super.merge(with);
		DeviceRequest dr = (DeviceRequest)with;
		if (devices==null) devices = dr.devices;
		if (devices!=null) devices.addAll(dr.devices);
		deviceName    = dr.deviceName;
		deviceModel   = dr.deviceModel;
		deviceType    = dr.deviceType;
		configure     = dr.configure;
		deviceAction  = dr.deviceAction;
		position      = dr.position;
		errorMessage  = dr.errorMessage;
	}

	
	public DeviceRequest() {
	
	}
	
	/**
	 * For IRunnableDeviceService.getRunnableDevice()
	 * @param name
	 */
	public DeviceRequest(String name) {
		deviceName = name;
	}
	
	/**
	 * For IRunnableDeviceService.getRunnableDevice()
	 * @param name
	 */
	public DeviceRequest(String name, DeviceAction action) {
		deviceName   = name;
		deviceAction = action;
	}


	/**
	 * For IRunnableDeviceService.createRunnableDevice()
	 * @param name
	 */
	public DeviceRequest(Object model, boolean conf) {
		this.deviceModel = model;
		this.configure   = conf;
	}
	
	/**
	 * For IRunnableDeviceService.getRunnableDevice(...)
	 * then device.configure(...)
	 * @param name
	 */
	public DeviceRequest(String name, Object model) {
		this.deviceName  = name;
		this.deviceModel = model;
	}

	public Collection<DeviceInformation<?>> getDevices() {
		return devices;
	}

	public void setDevices(Collection<DeviceInformation<?>> devices) {
		this.devices = devices;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (configure ? 1231 : 1237);
		result = prime * result + ((deviceAction == null) ? 0 : deviceAction.hashCode());
		result = prime * result + ((deviceModel == null) ? 0 : deviceModel.hashCode());
		result = prime * result + ((deviceName == null) ? 0 : deviceName.hashCode());
		result = prime * result + ((deviceType == null) ? 0 : deviceType.hashCode());
		result = prime * result + ((devices == null) ? 0 : devices.hashCode());
		result = prime * result + ((errorMessage == null) ? 0 : errorMessage.hashCode());
		result = prime * result + ((position == null) ? 0 : position.hashCode());
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
		if (configure != other.configure)
			return false;
		if (deviceAction != other.deviceAction)
			return false;
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
		if (deviceType != other.deviceType)
			return false;
		if (devices == null) {
			if (other.devices != null)
				return false;
		} else if (!devices.equals(other.devices))
			return false;
		if (errorMessage == null) {
			if (other.errorMessage != null)
				return false;
		} else if (!errorMessage.equals(other.errorMessage))
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
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

	public int size() {
		return devices==null ? 0 : devices.size();
	}

	public DeviceType getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}

	public boolean isConfigure() {
		return configure;
	}

	public void setConfigure(boolean configure) {
		this.configure = configure;
	}

	public DeviceAction getDeviceAction() {
		return deviceAction;
	}

	public void setDeviceAction(DeviceAction deviceAction) {
		this.deviceAction = deviceAction;
	}

	public IPosition getPosition() {
		return position;
	}

	public void setPosition(IPosition position) {
		this.position = position;
	}


	public String getErrorMessage() {
		return errorMessage;
	}


	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
