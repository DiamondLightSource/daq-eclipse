package org.eclipse.scanning.api.points;

public class PointsValidationException extends RuntimeException {
	// Use an unchecked exception because IPointGenerator.iterator() cannot
	// throw a checked exception. (TODO: Why can't we change signature of
	// IPointGenerator.iterator())?

	private static final long serialVersionUID = -2818058720202899355L;

	public PointsValidationException() {
		super();
	}

	public PointsValidationException(String message) {
		super(message);
	}

	public PointsValidationException(Throwable cause) {
		super(cause);
	}

	public PointsValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public PointsValidationException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
