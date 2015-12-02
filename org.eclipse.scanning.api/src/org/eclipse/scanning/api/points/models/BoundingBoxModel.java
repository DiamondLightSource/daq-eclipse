package org.eclipse.scanning.api.points.models;

public class BoundingBoxModel implements IBoundingBoxModel {

	private IPointModel startPoint = new PointModel();
	private double angle;
	private double xLength;
	private double yLength;
	private boolean isParentRectangle;

	@Override
	public double getAngle() {
		return angle;
	}
	@Override
	public void setAngle(double angle) {
		this.angle = angle;
	}
	@Override
	public double getxLength() {
		return xLength;
	}
	@Override
	public void setxLength(double xLength) {
		this.xLength = xLength;
	}
	@Override
	public double getyLength() {
		return yLength;
	}
	@Override
	public void setyLength(double yLength) {
		this.yLength = yLength;
	}
	@Override
	public boolean isParentRectangle() {
		return isParentRectangle;
	}
	@Override
	public void setParentRectangle(boolean isParentRectangle) {
		this.isParentRectangle = isParentRectangle;
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
		result = prime * result + (isParentRectangle ? 1231 : 1237);
		result = prime * result
				+ ((startPoint == null) ? 0 : startPoint.hashCode());
		temp = Double.doubleToLongBits(xLength);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yLength);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		BoundingBoxModel other = (BoundingBoxModel) obj;
		if (Double.doubleToLongBits(angle) != Double
				.doubleToLongBits(other.angle))
			return false;
		if (isParentRectangle != other.isParentRectangle)
			return false;
		if (startPoint == null) {
			if (other.startPoint != null)
				return false;
		} else if (!startPoint.equals(other.startPoint))
			return false;
		if (Double.doubleToLongBits(xLength) != Double
				.doubleToLongBits(other.xLength))
			return false;
		if (Double.doubleToLongBits(yLength) != Double
				.doubleToLongBits(other.yLength))
			return false;
		return true;
	}
}
