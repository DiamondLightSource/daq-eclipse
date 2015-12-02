package org.eclipse.scanning.api.points.models;

public class BoundingBoxModel implements IBoundingBoxModel {

	private double xStart;
	private double yStart;
	private double xLength;
	private double yLength;
	private double angle;
	private boolean isParentRectangle;

	public double getxStart() {
		return xStart;
	}
	public void setxStart(double xStart) {
		this.xStart = xStart;
	}
	public double getyStart() {
		return yStart;
	}
	public void setyStart(double yStart) {
		this.yStart = yStart;
	}
	public double getxLength() {
		return xLength;
	}
	public void setxLength(double xLength) {
		this.xLength = xLength;
	}
	public double getyLength() {
		return yLength;
	}
	public void setyLength(double yLength) {
		this.yLength = yLength;
	}
	public double getAngle() {
		return angle;
	}
	public void setAngle(double angle) {
		this.angle = angle;
	}
	public boolean isParentRectangle() {
		return isParentRectangle;
	}
	public void setParentRectangle(boolean isParentRectangle) {
		this.isParentRectangle = isParentRectangle;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(angle);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (isParentRectangle ? 1231 : 1237);
		temp = Double.doubleToLongBits(xLength);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(xStart);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yLength);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yStart);
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
		if (Double.doubleToLongBits(xLength) != Double
				.doubleToLongBits(other.xLength))
			return false;
		if (Double.doubleToLongBits(xStart) != Double
				.doubleToLongBits(other.xStart))
			return false;
		if (Double.doubleToLongBits(yLength) != Double
				.doubleToLongBits(other.yLength))
			return false;
		if (Double.doubleToLongBits(yStart) != Double
				.doubleToLongBits(other.yStart))
			return false;
		return true;
	}

}
