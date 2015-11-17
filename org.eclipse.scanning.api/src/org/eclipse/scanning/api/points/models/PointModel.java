package org.eclipse.scanning.api.points.models;

public class PointModel {
	
	private double x;
	private double y;
	
	private double angle;

	protected boolean lock;

	public double getX() {
		return x;
	}
	public void setX(double minX) {
		if (lock) throw new IllegalArgumentException("The model is locked and cannot be edited!");
		this.x = minX;
	}
	public double getY() {
		return y;
	}
	public void setY(double minY) {
		if (lock) throw new IllegalArgumentException("The model is locked and cannot be edited!");
		this.y = minY;
	}
	
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(angle);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (lock ? 1231 : 1237);
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
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
		PointModel other = (PointModel) obj;
		if (Double.doubleToLongBits(angle) != Double
				.doubleToLongBits(other.angle))
			return false;
		if (lock != other.lock)
			return false;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}

	public boolean isLock() {
		return lock;
	}

	public void setLock(boolean lock) {
		this.lock = lock;
	}

}
