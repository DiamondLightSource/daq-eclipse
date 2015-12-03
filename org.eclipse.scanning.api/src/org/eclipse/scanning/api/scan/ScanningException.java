package org.eclipse.scanning.api.scan;

public class ScanningException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2587074494060854407L;

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

	public ScanningException(Throwable cause) {
		super(cause);
	}

}
