package org.eclipse.scanning.api.points.models;

import org.eclipse.scanning.api.annotation.UiTooltip;
import org.eclipse.scanning.api.annotation.Units;

public class RandomOffsetGridModel extends GridModel {

	@Override
	public String getName() {
		return "Random Offset Grid";
	}
	
	/**
	 * The maximum allowed offset, as a percentage of fast axis step size
	 */
	private double offset;
	/**
	 * Seed to initialise random number generator with
	 */
	private int seed;

	@Units("%")
	@UiTooltip("Standard deviation to use for a 2D Gaussian random offset, as a percentage of the X step size")
	public double getOffset() {
		return offset;
	}
	public void setOffset(double newValue) {
		double oldValue = this.offset;
		this.offset = newValue;
		this.pcs.firePropertyChange("offset", oldValue, newValue);
	}
	@UiTooltip("Seed to initialise random number generator with")
	public int getSeed() {
		return seed;
	}
	public void setSeed(int newValue) {
		double oldValue = this.seed;
		this.seed = newValue;
		this.pcs.firePropertyChange("seed", oldValue, newValue);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(offset);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		RandomOffsetGridModel other = (RandomOffsetGridModel) obj;
		if (Double.doubleToLongBits(offset) != Double.doubleToLongBits(other.offset))
			return false;
		return true;
	}
}
