package org.eclipse.scanning.api;

import org.eclipse.scanning.api.annotation.UiHidden;

public interface ITimeoutable {

	/**
	 * This is the timout time which defaults to
	 * -1
	 * 
	 * If set the default timeout for an action on a device 
	 * will use this value. For instance for detectors the run
	 * and write time will timeout if this field is set>0 or
	 * 10 seconds if none of the detetor models have this field
	 * set. For scannables the IScannable interface extends this
	 * interface. If any motor at a given level implements this
	 * timeout, this time out (or the max of all the timeouts)
	 * will be used. If none are set the default is three minutes.
	 * 
	 * @return
	 */
	@UiHidden
	default long getTimeout() {
		return -1;
	}
	
	/**
	 * 
	 * @param time in seconds
	 */
	@UiHidden
	default void setTimeout(long time) {
		throw new IllegalArgumentException("setExposureTime(...) is not implemented");
	}

}
