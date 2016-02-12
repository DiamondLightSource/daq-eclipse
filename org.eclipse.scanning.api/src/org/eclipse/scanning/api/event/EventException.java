package org.eclipse.scanning.api.event;

public class EventException extends Exception {

	private static final long serialVersionUID = -6644630244716886256L;

	public EventException() {
		super();
	}

	public EventException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public EventException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public EventException(String message) {
		super(message);
	}

	public EventException(Throwable throwable) {
		super(throwable);
	}

}
