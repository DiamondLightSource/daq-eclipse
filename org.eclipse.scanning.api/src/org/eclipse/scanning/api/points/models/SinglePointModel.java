package org.eclipse.scanning.api.points.models;

public class SinglePointModel implements IPointModel {

	private IPointModel point = new PointModel();

	@Override
	public double getX() {
		return point.getX();
	}
	@Override
	public void setX(double minX) {
		point.setX(minX);
	}
	@Override
	public double getY() {
		return point.getY();
	}
	@Override
	public void setY(double minY) {
		point.setY(minY);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((point == null) ? 0 : point.hashCode());
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
		SinglePointModel other = (SinglePointModel) obj;
		if (point == null) {
			if (other.point != null)
				return false;
		} else if (!point.equals(other.point))
			return false;
		return true;
	}
}
