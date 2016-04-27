package org.eclipse.scanning.command;

public class PyExpressionNotImplementedException extends Exception {

	private static final long serialVersionUID = 6008079465533652603L;

	public PyExpressionNotImplementedException() { }

	public PyExpressionNotImplementedException(String message) {
		super(message);
	}

	public PyExpressionNotImplementedException(Throwable cause) {
		super(cause);
	}

	public PyExpressionNotImplementedException(String message,
			Throwable cause) {
		super(message, cause);
	}

	public PyExpressionNotImplementedException(String message,
			Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
