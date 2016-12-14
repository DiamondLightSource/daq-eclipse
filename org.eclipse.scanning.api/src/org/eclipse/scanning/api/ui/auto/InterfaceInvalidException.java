package org.eclipse.scanning.api.ui.auto;

public class InterfaceInvalidException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8741038493600761528L;

	public InterfaceInvalidException() {
		super();
	}

	public InterfaceInvalidException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InterfaceInvalidException(String message, Throwable cause) {
		super(message, cause);
	}

	public InterfaceInvalidException(String message) {
		super(message);
	}

	public InterfaceInvalidException(Throwable cause) {
		super(cause);
	}

}
