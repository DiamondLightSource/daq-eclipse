package org.eclipse.scanning.api.scan;


public interface IResetableDevice {

	/**
	 * Allowed from Fault. Will try to reset the device into Idle state. Will block until the device is in a rest state.
	 * @throws ScanningException 
	 */
	public void reset() throws ScanningException; 

}
