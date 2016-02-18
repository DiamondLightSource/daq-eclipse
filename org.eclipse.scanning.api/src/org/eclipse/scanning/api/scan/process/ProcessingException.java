package org.eclipse.scanning.api.scan.process;

import org.eclipse.scanning.api.event.EventException;

public class ProcessingException extends EventException {

	public ProcessingException() {
		super();
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
