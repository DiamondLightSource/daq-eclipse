package org.eclipse.scanning.api.device;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.scanning.api.IScannable;
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
	 * Returns the set of global metadata scannable names that should be added to all scans.
	 * This is used to support legacy (GDA8) spring configurations. Should not be called
	 * by client code.
	 * @return global metadata scannable names
	 */
	@Deprecated
	default Set<String> getGlobalMetadataScannableNames() {
		return Collections.emptySet();
	}
	
	/**
	 * Returns the set of the names required metadata scannables for the given scannable name.
	 * This is used to support legacy (GDA8) spring configurations. Should not be called
	 * by client code. 
	 * @param scannableName scannable to get required metadata scannable names for
	 * @return names of required metadata scannables for the scannable with the given name
	 */
	@Deprecated
	default Set<String> getRequiredMetadataScannableNames(String scannableName) {
		return Collections.emptySet();
	}
	
}
