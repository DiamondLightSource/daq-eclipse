package org.eclipse.scanning.api.device.legacy;

import java.util.Set;

/**
 * Interface for legacy device support. This interface serves to partially support
 * legacy (GDA8) spring configurations, in particular metadata scannables and location maps.
 * 
 * Client code should not use this interface directly, it should only be used by the
 * nexus writing framework. It is a temporary measure to allow support of legacy spring
 * configurations and will be removed in a future release.
 * 
 * @author Matthew Dickie
 */
public interface ILegacyDeviceSupport {
	
	/**
	 * Set the names of the metadata scannables to use in scans. A metadata scannable
	 * is a scannable that only writes its data once, at the start of the scan.
	 * @param metadataScannableNames metadata scannable names
	 */
	public void setMetadataScannableNames(Set<String> metadataScannableNames);
	
	/**
	 * Returns the name of the metadata scannables to use.
	 * @return names of metadata scannables, never <code>null</code>
	 */
	public Set<String> getMetadataScannableNames();
	
	/**
	 * Gets the names of the devices configured with a {@link LegacyDeviceConfig}.
	 * @return name of configured legacy devices, never <code>null</code>
	 */
	public Set<String> getLegacyDeviceNames();
	
	/**
	 * Returns the {@link LegacyDeviceConfig} object for the legacy device with the given name,
	 * or <code>null</code> if no such config exists
	 * @param deviceName device name
	 * @return legacy device config for the device with the given name, or
	 *     <code>null</code> if no such object exists
	 */
	public LegacyDeviceConfig getLegacyDeviceConfig(String deviceName);
	
	/**
	 * Sets the {@link LegacyDeviceConfig} for the device with the given name
	 * @param deviceName device name
	 * @param legacyDeviceConfig device config
	 */
	public void setLegacyDeviceConfig(String deviceName, LegacyDeviceConfig legacyDeviceConfig);

}
