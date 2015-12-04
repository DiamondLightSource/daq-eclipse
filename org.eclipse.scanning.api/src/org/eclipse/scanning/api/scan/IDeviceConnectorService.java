package org.eclipse.scanning.api.scan;

import org.eclipse.scanning.api.IScannable;

/**
 * 
 * Clients do not need to consume this service, it is used
 * to provide connection to devices which already exist, like those in GFA8.
 * 
 * A bundle implements this service and the scanning
 * consumes the service. The tests provide the service
 * directly.
 * 
 * This service is designed to be implemented by GDA8 to 
 * provide scannables (existing ones) to the new system.
 * This can be done easily as IScannable is a subset of
 * Scannable in GDA8.
 * 
 * It also provides IDetectors. These are not analogous but
 * not the same as Detector in GDA. The service wraps both gda.device.Detector
 * into IReadableDetector and gda.px.detector.Detector into IScanner. In the
 * first case the model provides the collection time and anything else required.
 * In the later case the model provides the file path and omegaStart required.
 * 
 * It mirrors the other connector services like the jms one,
 * which push the details of getting connections outside the
 * core scanning API using declarative services.
 * 
 * @author Matthew Gerring
 *
 */
public interface IDeviceConnectorService {

	/**
	 * 
	 * @param name
	 * @return
	 * @throws ScanningException
	 */
	<T> IScannable<T> getScannable(String name) throws ScanningException;

	
	/**
	 * 
	 * @param name
	 * @return
	 * @throws ScanningException
	 */
	<U> IRunnableDevice<U> getDetector(String name) throws ScanningException;

}
