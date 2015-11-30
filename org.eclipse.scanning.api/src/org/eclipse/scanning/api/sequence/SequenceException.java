package org.eclipse.scanning.api.sequence;

public class SequenceException extends Exception {

	public SequenceException() {
		super();
	}

	public SequenceException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SequenceException(String message, Throwable cause) {
		super(message, cause);
    }

	public SequenceException(String message) {
		super(message);
	}

	public SequenceException(Throwable cause) {
		super(cause);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 2587074494060854407L;

}
