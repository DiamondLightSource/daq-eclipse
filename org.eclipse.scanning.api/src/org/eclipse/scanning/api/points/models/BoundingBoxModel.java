package org.eclipse.scanning.api.points.models;

public class BoundingBoxModel {

	private double minX;
	private double minY;

	private double xLength;
	private double yLength;
	
	private double angle;
	
	private boolean isParentRectangle;
	private boolean lock;
	
	/**
	 * @return angle, in radians
	 */
	public double getAngle() {
		return angle;
	}

	/**
	 * Set angle, in radians, from 0 to pi/2 is from x-axis to y-axis
	 * @param angle from 0 to pi/2 is from x-axis to y-axis
	 */
	public void setAngle(double angle) {
		if (lock) throw new IllegalArgumentException("The model is locked and cannot be edited!");
		this.angle = angle;
	}
	
	public double getMinX() {
		return minX;
	}
	public void setMinX(double minX) {
		if (lock) throw new IllegalArgumentException("The model is locked and cannot be edited!");
		this.minX = minX;
	}
	public double getMinY() {
		return minY;
	}
	public void setMinY(double minY) {
		if (lock) throw new IllegalArgumentException("The model is locked and cannot be edited!");
		this.minY = minY;
	}
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
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(angle);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (isParentRectangle ? 1231 : 1237);
		result = prime * result + (lock ? 1231 : 1237);
		temp = Double.doubleToLongBits(minX);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(minY);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		if (lock != other.lock)
			return false;
		if (Double.doubleToLongBits(minX) != Double
				.doubleToLongBits(other.minX))
			return false;
		if (Double.doubleToLongBits(minY) != Double
				.doubleToLongBits(other.minY))
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

	public boolean isLock() {
		return lock;
	}

	public void setLock(boolean lock) {
		this.lock = lock;
	}
}
