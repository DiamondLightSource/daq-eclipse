package org.eclipse.scanning.api.scan.event;

import java.util.EventObject;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.IRunnableDevice;

public class RunEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8344532962994793917L;

	private IPosition position;

	public RunEvent(IRunnableDevice<?> device, IPosition position) {
		super(device);
		this.position = position;
	}

	public IRunnableDevice<?> getDevice() {
		return (IRunnableDevice<?>)getSource();
	}
	
	public IPosition getPosition() {
		return position;
	}
}
