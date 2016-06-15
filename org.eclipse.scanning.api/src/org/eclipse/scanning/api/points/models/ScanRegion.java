package org.eclipse.scanning.api.points.models;

import java.util.Arrays;
import java.util.List;

/**
 * 
 * A scan region encapsulates a geometric region of interest with
 * the names of the scan axes over which it is a region.
 * 
 * @author Matthew Gerring
 *
 * @param <T>
 */
public class ScanRegion<T> {

	private T            roi;
	private List<String> scannables;
	
	public ScanRegion() {
		// We are a bean
	}
	
	public ScanRegion(T roi, List<String> names) {
		this.roi = roi;
		this.scannables = names;
	}
	public ScanRegion(T roi, String... names) {
		this.roi = roi;
		this.scannables = Arrays.asList(names);
	}
	
	public T getRoi() {
		return roi;
	}
	public void setRoi(T roi) {
		this.roi = roi;
	}
	public List<String> getScannables() {
		return scannables;
	}
	public void setScannables(List<String> scannables) {
		this.scannables = scannables;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((roi == null) ? 0 : roi.hashCode());
		result = prime * result + ((scannables == null) ? 0 : scannables.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScanRegion other = (ScanRegion) obj;
		if (roi == null) {
			if (other.roi != null)
				return false;
		} else if (!roi.equals(other.roi))
			return false;
		if (scannables == null) {
			if (other.scannables != null)
				return false;
		} else if (!scannables.equals(other.scannables))
			return false;
		return true;
	}
}
