package org.eclipse.scanning.api.points.models;

public class LinearModel implements ILinearModel {

	private IPointModel startPoint = new PointModel();
	private double angle;
	private double length;

	@Override
	public double getAngle() {
		return angle;
	}
	@Override
	public void setAngle(double angle) {
		this.angle = angle;
	}
	@Override
	public double getLength() {
		return length;
	}
	@Override
	public void setLength(double length) {
		this.length = length;
	}
	@Override
	public double getxStart() {
		return startPoint.getX();
	}
	@Override
	public void setxStart(double minX) {
		startPoint.setX(minX);
	}
	@Override
	public double getyStart() {
		return startPoint.getY();
	}
	@Override
	public void setyStart(double minY) {
		startPoint.setY(minY);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(angle);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(length);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((startPoint == null) ? 0 : startPoint.hashCode());
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
		LinearModel other = (LinearModel) obj;
		if (Double.doubleToLongBits(angle) != Double
				.doubleToLongBits(other.angle))
			return false;
		if (Double.doubleToLongBits(length) != Double
				.doubleToLongBits(other.length))
			return false;
		if (startPoint == null) {
			if (other.startPoint != null)
				return false;
		} else if (!startPoint.equals(other.startPoint))
			return false;
		return true;
	}
}
