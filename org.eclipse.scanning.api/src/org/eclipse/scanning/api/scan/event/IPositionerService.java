package org.eclipse.scanning.api.scan.event;

import org.eclipse.scanning.api.scan.ScanningException;

/**
 * 
 * Interface for creating positioners. Normally
 * 
 * This is done by getting the IRunnableDeviceService which
 * is usually available and calling createPositioner() on that.
 * 
 * @author Matthew Gerring
 *
 */
public interface IPositionerService {

	
	/**
	 * This method sets the value of the scannables named to this position.
	 * It takes into account the levels of the scannbles. 
	 * It is blocking until all the scannables have reached the desired location.
	 * 
	 * @return
	 * @throws ScanningException
	 */
	IPositioner createPositioner() throws ScanningException;

}
