package org.eclipse.scanning.api.device;

import org.eclipse.scanning.api.scan.ScanningException;


public interface IResettableDevice {

	/**
	 * Allowed from Fault. Will try to reset the device into Idle state. Will block until the device is in a rest state.
	 * @throws ScanningException 
	 */
	public void reset() throws ScanningException; 

}
