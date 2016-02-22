package org.eclipse.scanning.api.points.models;

import java.util.Arrays;


public class ArrayModel extends AbstractPointsModel implements IScanPathModel {

	private String name;
	private double[] positions;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		String oldValue = this.name;
		this.name = name;
		this.pcs.firePropertyChange("name", oldValue, name);
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
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (!Arrays.equals(positions, other.positions))
			return false;
		return true;
	}
}
