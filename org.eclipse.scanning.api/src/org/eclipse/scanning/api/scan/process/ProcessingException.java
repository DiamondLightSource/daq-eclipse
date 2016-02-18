package org.eclipse.scanning.api.scan.process;

import org.eclipse.scanning.api.scan.IRunnableDevice;
import org.eclipse.scanning.api.scan.ScanningException;

public class ProcessingException extends ScanningException {

	public ProcessingException() {
		super();
	}

	public ProcessingException(IRunnableDevice<?> device, String message) {
		super(device, message);
	}

	public ProcessingException(IRunnableDevice<?> device, Throwable cause) {
		super(device, cause);
	}

	public ProcessingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		}

	public ProcessingException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProcessingException(String message) {
		super(message);
	}

	public ProcessingException(Throwable cause) {
		super(cause);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -3056159047121974655L;

}
