package org.eclipse.scanning.api;

import org.eclipse.scanning.api.device.IActivatable;
import org.eclipse.scanning.api.scan.ScanningException;

/**
 * A monitored device is any activatable device which may participate 
 * in the scan. It has different monitor roles, either 
 * 
 * @author Matthew Gerring
 *
 */
public interface IMonitoredDevice extends IActivatable {

	default MonitorRole getMonitorRole() {
		return MonitorRole.PER_POINT;
	}
	
	default MonitorRole setMonitorRole(MonitorRole newType) throws ScanningException {
		throw new IllegalArgumentException("The monitor type cannot be set on "+getClass().getSimpleName());
	}
}
