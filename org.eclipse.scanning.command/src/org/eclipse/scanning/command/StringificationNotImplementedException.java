package org.eclipse.scanning.command;

public class StringificationNotImplementedException extends Exception {

	private static final long serialVersionUID = 6008079465533652603L;

	public StringificationNotImplementedException() { }

	public StringificationNotImplementedException(String message) {
		super(message);
	}

	public StringificationNotImplementedException(Throwable cause) {
		super(cause);
	}

	public StringificationNotImplementedException(String message,
			Throwable cause) {
		super(message, cause);
	}

	public StringificationNotImplementedException(String message,
			Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
