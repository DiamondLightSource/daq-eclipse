package org.eclipse.scanning.api.points;

public class GeneratorException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2484455579381036697L;

	public GeneratorException() {
		super();
	}

	public GeneratorException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public GeneratorException(String message, Throwable cause) {
		super(message, cause);
	}

	public GeneratorException(String message) {
		super(message);
	}

	public GeneratorException(Throwable cause) {
		super(cause);
	}

}
