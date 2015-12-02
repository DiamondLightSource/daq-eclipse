package org.eclipse.scanning.api.points.models;

public interface IBoundingBoxModel {

	public double getxStart();

	public void setxStart(double xStart);

	public double getyStart();

	public void setyStart(double yStart);

	public double getxLength();

	public void setxLength(double xLength);

	public double getyLength();

	public void setyLength(double yLength);

	/**
	 * @return angle, in radians
	 */
	public double getAngle();

	/**
	 * Set angle, in radians, from 0 to pi/2 is from x-axis to y-axis
	 * @param angle from 0 to pi/2 is from x-axis to y-axis
	 */
	public void setAngle(double angle);

	public boolean isParentRectangle();

	public void setParentRectangle(boolean isParentRectangle);

	public int hashCode();

	public boolean equals(Object obj);

}