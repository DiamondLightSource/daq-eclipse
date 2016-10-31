package org.eclipse.scanning.api.device.models;

import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;

public abstract class AbstractDetectorModel implements IDetectorModel {

	/**
	 * The name of the detector device
	 */
	@FieldDescriptor(label="Name")
	private String name;

	/**
	 * The exposure time. If calculation is shorter than this, time is artificially added to make the detector respect
	 * the time that is set.
	 */
	@FieldDescriptor(label="Exposure Time", unit="s", minimum=0)
	private double exposureTime; // Seconds

	public double getExposureTime() {
		return exposureTime;
	}

	public void setExposureTime(double exposureTime) {
		this.exposureTime = exposureTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(exposureTime);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		AbstractDetectorModel other = (AbstractDetectorModel) obj;
		if (Double.doubleToLongBits(exposureTime) != Double.doubleToLongBits(other.exposureTime))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
