package org.eclipse.scanning.api.points.models;

public class BoundingBoxModel extends PointModel {

	private double xLength;
	private double yLength;
	
	private boolean isParentRectangle;
	
	
	public double getxLength() {
		return xLength;
	}
	public void setxLength(double xLength) {
		if (lock) throw new IllegalArgumentException("The model is locked and cannot be edited!");
		this.xLength = xLength;
	}
	public double getyLength() {
		return yLength;
	}
	public void setyLength(double yLength) {
		if (lock) throw new IllegalArgumentException("The model is locked and cannot be edited!");
		this.yLength = yLength;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (isParentRectangle ? 1231 : 1237);
		long temp;
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
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		BoundingBoxModel other = (BoundingBoxModel) obj;
		if (isParentRectangle != other.isParentRectangle)
			return false;
		if (Double.doubleToLongBits(xLength) != Double
				.doubleToLongBits(other.xLength))
			return false;
		if (Double.doubleToLongBits(yLength) != Double
				.doubleToLongBits(other.yLength))
			return false;
		return true;
	}

	public boolean isParentRectangle() {
		return isParentRectangle;
	}

	public void setParentRectangle(boolean isParentRectangle) {
		if (lock) throw new IllegalArgumentException("The model is locked and cannot be edited!");
		this.isParentRectangle = isParentRectangle;
	}
}
