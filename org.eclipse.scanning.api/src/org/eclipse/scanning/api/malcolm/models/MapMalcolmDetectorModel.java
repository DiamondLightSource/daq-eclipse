package org.eclipse.scanning.api.malcolm.models;

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
public class MapMalcolmDetectorModel extends MalcolmDetectorConfiguration<Map> {

	private static final String EXPOSURE_NAME = "exposure";

	public double getExposureTime() {
		Object exposure = getModel().get(EXPOSURE_NAME);
		if (exposure instanceof Number) {
			return ((Number) exposure).doubleValue();
		} else {
			return 0.0;
		}
	}
	
	public MapMalcolmDetectorModel() {
		
	}

	public MapMalcolmDetectorModel(Map model) {
		setModel(model);
	}
	public void setExposureTime(double exposureTime) {
		getModel().put(EXPOSURE_NAME, Double.valueOf(exposureTime));
	}

	public Map<String, Object> getParameterMap() {
		return getModel();
	}

	public void setParameterMap(Map<String, Object> parameterMap) {
		setModel(parameterMap);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getModel() == null) ? 0 : getModel().hashCode());
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
		MapMalcolmDetectorModel other = (MapMalcolmDetectorModel) obj;
		if (getModel() == null) {
			if (other.getModel() != null)
				return false;
		} else if (!getModel().equals(other.getModel()))
			return false;
		return true;
	}
}
