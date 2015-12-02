package org.eclipse.scanning.api.scan;

import org.eclipse.scanning.api.IScannable;

/**
 * 
 * A service to create sequencers for different scan types.
 * 
 * @author fcp94556
 *
 */
public interface IScanningService {

	/**
	 * Create an empty scanner which can run an iterable to complete a scan.
	 * @return
	 * @throws ScanningException
	 */
	IScanner createScanner() throws ScanningException;
	
	/**
	 * 
	 * @param name
	 * @return
	 * @throws ScanningException
	 */
	<T> IScannable<T> getScannable(String name) throws ScanningException;
}
