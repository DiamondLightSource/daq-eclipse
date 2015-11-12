package org.eclipse.malcolm.api;

import java.io.IOException;

public class MalcolmDeviceException extends IOException {

	private final IMalcolmDevice device;

	public MalcolmDeviceException(String message) {
		this(null, message);
	}
	public MalcolmDeviceException(IMalcolmDevice device) {
		this(device, (String)null);
	}
	public MalcolmDeviceException(IMalcolmDevice device, String message) {
		this(device, message, null);
	}
	public MalcolmDeviceException(IMalcolmDevice device, String message, Throwable original) {
		super(message, original);
		this.device = device;
	}

	public MalcolmDeviceException(IMalcolmDevice device, Exception e) {
		super(e);
		this.device = device;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -2394321108005036591L;

	public IMalcolmDevice getDevice() {
		return device;
	}

}
