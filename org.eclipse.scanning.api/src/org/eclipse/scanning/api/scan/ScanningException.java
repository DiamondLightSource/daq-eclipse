package org.eclipse.scanning.api.scan;

public class ScanningException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2587074494060854407L;
	private IRunnableDevice<?> device;

	public ScanningException() {
		super();
	}

	public ScanningException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ScanningException(String message, Throwable cause) {
		super(message, cause);
    }

	public ScanningException(String message) {
		super(message);
	}
	public ScanningException(IRunnableDevice<?> device, String message) {
		super(message);
		this.device = device;
	}

	public ScanningException(Throwable cause) {
		super(cause);
	}
	public ScanningException(IRunnableDevice<?> device, Throwable cause) {
		super(cause);
		this.device = device;
	}

	/**
	 *  
	 * @return the device, may be null.
	 */
	public IRunnableDevice<?> getDevice() {
		return device;
	}

	public void setDevice(IRunnableDevice<?> device) {
		this.device = device;
	}

}
