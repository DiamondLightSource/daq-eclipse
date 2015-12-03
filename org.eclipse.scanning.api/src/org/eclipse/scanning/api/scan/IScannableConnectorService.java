package org.eclipse.scanning.api.scan;

import org.eclipse.scanning.api.IScannable;

/**
 * 
 * This service is designed to be implemented by GDA8 to 
 * provide scannables (existing ones) to the new system.
 * 
 * It mirrors the other connector services like the jms one,
 * which push the details of getting connections outside the
 * core scanning API using declarative services.
 * 
 * @author Matthew Gerring
 *
 */
public interface IScannableConnectorService {

	/**
	 * 
	 * @param name
	 * @return
	 * @throws ScanningException
	 */
	<T> IScannable<T> getScannable(String name) throws ScanningException;

}
