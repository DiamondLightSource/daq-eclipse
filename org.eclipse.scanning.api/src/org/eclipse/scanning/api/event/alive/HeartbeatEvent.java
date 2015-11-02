package org.eclipse.scanning.api.event.alive;

import java.util.EventObject;

public class HeartbeatEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -120191399800024906L;


	public HeartbeatEvent(HeartbeatBean source) {
		super(source);
	}

	public HeartbeatBean getBean() {
		return (HeartbeatBean)getSource();
	}
}
