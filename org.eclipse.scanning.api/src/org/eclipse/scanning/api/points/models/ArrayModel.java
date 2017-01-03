package org.eclipse.scanning.api.points.models;

import java.util.Arrays;


/**
 * A model for a scan at an array of defined positions along a single axis.
 *
 * @author Colin Palmer
 *
 */
public class ArrayModel extends AbstractPointsModel implements IScanPathModel {
	

	private double[] positions;

	public ArrayModel() {
		
	}
	
	public ArrayModel(double... positions) {
		this.positions = positions;
	}

	public double[] getPositions() {
		return positions;
	}
	public void setPositions(double... positions) {
		double[] oldValue = this.positions;
		this.positions = positions;
		this.pcs.firePropertyChange("positions", oldValue, positions);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(positions);
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
		ArrayModel other = (ArrayModel) obj;
		if (!Arrays.equals(positions, other.positions))
			return false;
		return true;
	}
}
