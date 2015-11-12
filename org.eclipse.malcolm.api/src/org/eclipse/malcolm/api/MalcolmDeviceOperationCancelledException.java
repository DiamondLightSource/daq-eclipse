package org.eclipse.malcolm.api;

public class MalcolmDeviceOperationCancelledException extends MalcolmDeviceException {
	public MalcolmDeviceOperationCancelledException(String message) {
		super(message);
	}
	public MalcolmDeviceOperationCancelledException(IMalcolmDevice device) {
		super(device);
	}
	public MalcolmDeviceOperationCancelledException(IMalcolmDevice device, String message) {
		super(device, message);
	}
	public MalcolmDeviceOperationCancelledException(IMalcolmDevice device, String message, Throwable original) {
		super(device, message, original);
	}
}
