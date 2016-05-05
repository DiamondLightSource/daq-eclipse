package org.eclipse.scanning.api.scan.event;

import java.util.EventObject;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPosition;

public class RunEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8344532962994793917L;

	private final IPosition position;
	
	private final DeviceState deviceState;

	public RunEvent(IRunnableDevice<?> device, IPosition position, DeviceState deviceState) {
		super(device);
		this.position = position;
		this.deviceState = deviceState;
	}

	public IRunnableDevice<?> getDevice() {
		return (IRunnableDevice<?>)getSource();
	}
	
	public IPosition getPosition() {
		return position;
	}
	
	public DeviceState getDeviceState() {
		return deviceState;
	}
	
}
