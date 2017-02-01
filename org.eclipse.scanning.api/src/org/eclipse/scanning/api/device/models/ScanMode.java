package org.eclipse.scanning.api.device.models;

/**
 * An enumeration of detector modes. This determines whether the detector can take
 * part in a GDA scan or a hardare (i.e. malcolm) scan. A detector may support
 * one or both of these modes.
 * 
 * @author Matthew Dickie
 */
public enum ScanMode {
	
	/**
	 * A scan that is run entirely in software by GDA
	 */
	SOFTWARE,
	
	/**
	 * A scan that is at least partially (an inner-scan) performed
	 * by hardware (i.e. malcolm) with GDA only performing an outer scan.
	 */
	HARDWARE

}
