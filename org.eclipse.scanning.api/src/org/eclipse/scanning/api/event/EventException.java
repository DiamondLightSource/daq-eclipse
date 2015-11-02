package org.eclipse.scanning.api.event;

public class EventException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6644630244716886256L;

	public EventException() {
		super();
	}

	public EventException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public EventException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public EventException(String arg0) {
		super(arg0);
	}

	public EventException(Throwable arg0) {
		super(arg0);	}

}
