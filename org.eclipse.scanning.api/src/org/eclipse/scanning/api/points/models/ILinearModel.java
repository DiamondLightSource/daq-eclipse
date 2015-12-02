package org.eclipse.scanning.api.points.models;

public interface ILinearModel {

	public double getxStart();

	public void setxStart(double xStart);

	public double getyStart();

	public void setyStart(double yStart);

	public double getLength();

	public void setLength(double length);

	/**
	 * @return angle, in radians
	 */
	public double getAngle();

	/**
	 * Set angle, in radians, from 0 to pi/2 is from x-axis to y-axis
	 * @param angle from 0 to pi/2 is from x-axis to y-axis
	 */
	public void setAngle(double angle);

	public int hashCode();

	public boolean equals(Object obj);

}