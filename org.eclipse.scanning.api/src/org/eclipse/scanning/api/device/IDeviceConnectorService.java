package org.eclipse.scanning.api.device;

import java.util.List;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.legacy.ILegacyDeviceSupport;
import org.eclipse.scanning.api.scan.ScanningException;

/**
 * Clients do not need to consume this service, it is used to provide connection
 * to devices which already exist, like those in GDA8.
 * <p>
 * A bundle implements this service and the scanning consumes the service. The
 * tests provide the service directly.
 * <p>
 * This service is designed to be implemented by GDA8 to provide scannables
 * (existing ones) to the new system. This can be done easily as
 * {@link IScannable} is a subset of Scannable in GDA8.
 * <p>
 * It also provides {@link IWritableDetector}'s. These are analogous to, but not
 * the same as Detector in GDA. The service wraps both gda.device.Detector into
 * {@link IWritableDetector} and gda.px.detector.Detector into
 * {@link IWritableDetector}. In the first case the model provides the
 * collection time and anything else required. In the later case the model
 * provides the file path and omegaStart required.
 * <p>
 * It mirrors the other connector services like the JMS one, which push the
 * details of getting connections outside the core scanning API using
 * declarative services.
 * 
 * @author Matthew Gerring
 *
 */
public interface IDeviceConnectorService {
	
	/**
	 * Get the names of all scannables known to the connector.
	 * @return
	 * @throws ScanningException
	 */
	List<String> getScannableNames() throws ScanningException;

	/**
	 * Get a scannable by name.
	 * @param name name of scannable to find
	 * @return scannable, never <code>null</code>
	 * @throws ScanningException if no scannable with the given name could be found
	 */
	<T> IScannable<T> getScannable(String name) throws ScanningException;
	
	/**
	 * Returns the {@link ILegacyDeviceSupport}. This provides a place for GDA9 to access
	 * legacy spring configuration settings for GDA8 scannables.
	 * May be <code>null</code> if legacy devices are not supported
	 * @return legacy device support
	 */
	ILegacyDeviceSupport getLegacyDeviceSupport();

}
