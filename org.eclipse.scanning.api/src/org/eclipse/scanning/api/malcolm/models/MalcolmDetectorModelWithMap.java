package org.eclipse.scanning.api.malcolm.models;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * A MalcolmDetectorModel implementation which contains a map of parameters. The (required) exposure time parameter is
 * stored in the map but has dedicated getter and setter methods.
 * <p>
 * This is intended to be a temporary model for use in testing, until we have dedicated classes for each Malcolm model
 *
 * @author Colin Palmer
 *
 */
public class MalcolmDetectorModelWithMap extends MalcolmDetectorConfiguration {

	private static final String EXPOSURE_NAME = "exposure";

	private Map<String, Object> parameterMap = new LinkedHashMap<>();

	public double getExposureTime() {
		Object exposure = parameterMap.get(EXPOSURE_NAME);
		if (exposure instanceof Number) {
			return ((Number) exposure).doubleValue();
		} else {
			return 0.0;
		}
	}

	public void setExposureTime(double exposureTime) {
		parameterMap.put(EXPOSURE_NAME, Double.valueOf(exposureTime));
	}

	public Map<String, Object> getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(Map<String, Object> parameterMap) {
		this.parameterMap = parameterMap;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((parameterMap == null) ? 0 : parameterMap.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MalcolmDetectorModelWithMap other = (MalcolmDetectorModelWithMap) obj;
		if (parameterMap == null) {
			if (other.parameterMap != null)
				return false;
		} else if (!parameterMap.equals(other.parameterMap))
			return false;
		return true;
	}
}
