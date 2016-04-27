package org.eclipse.scanning.api.device.models;

import org.eclipse.scanning.api.ITimeoutable;
import org.eclipse.scanning.api.annotation.MinimumValue;


/**
 * Interface to be implemented by all detector models, to ensure they have an exposure time.
 *
 * @author Colin Palmer
 *
 */
public interface IDetectorModel extends ITimeoutable {

	/**
	 * Get the exposure time to be used for the detector, in seconds.
	 *
	 * @return the exposure time in seconds. Can be zero but not negative.
	 */
	@MinimumValue("0")
	public double getExposureTime();
	
}
