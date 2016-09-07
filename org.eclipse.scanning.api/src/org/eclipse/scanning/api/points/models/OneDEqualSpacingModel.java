package org.eclipse.scanning.api.points.models;

import org.eclipse.scanning.api.annotation.MinimumValue;

/**
 * A model for a scan along a straight line in two-dimensional space, dividing the line into the number of points given
 * in this model.
 *
 * @author Colin Palmer
 *
 */
public class OneDEqualSpacingModel extends AbstractBoundingLineModel implements IBoundingLineModel {

	private int points = 5;

	public OneDEqualSpacingModel() {
		setName("Equal Spacing");
	}
	
	@MinimumValue("1")
	public int getPoints() {
		return points;
	}
	public void setPoints(int points) {
		int oldValue = this.points;
		this.points = points;
		this.pcs.firePropertyChange("points", oldValue, points);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + points;
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
		OneDEqualSpacingModel other = (OneDEqualSpacingModel) obj;
		if (points != other.points)
			return false;
		return true;
	}
}
