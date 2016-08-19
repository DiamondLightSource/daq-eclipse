package org.eclipse.scanning.api.scan.event;

import java.util.EventObject;

public class LocationEvent extends EventObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5183358734729833586L;

	public LocationEvent(Location source) {
		super(source);
	}

	public Location getLocation() {
		return (Location)getSource();
	}

}
