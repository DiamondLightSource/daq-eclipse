package org.eclipse.scanning.api.points.models;


/**
 * A model defining a line in two dimensional space, which can be used to confine and give scale to a {@link
 * IBoundingLineModel}.
 * <p>
 * The position of the line is defined by an (X, Y) start point, an angle (CCW from the positive X axis) and a length.
 * However, X and Y do not necessarily need to refer to two orthogonal physical axes.
 *
 * @author Colin Palmer
 *
 */
public class BoundingLine {

	private double xStart;
	private double yStart;
	private double angle;
	private double length;

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
	public double getAngle() {
		return angle;
	}
	public void setAngle(double angle) {
		this.angle = angle;  // FIXME: Degrees? Radians?
	}
	public double getLength() {
		return length;
	}
	public void setLength(double length) {
		this.length = length;
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
		temp = Double.doubleToLongBits(xStart);
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
		BoundingLine other = (BoundingLine) obj;
		if (Double.doubleToLongBits(angle) != Double
				.doubleToLongBits(other.angle))
			return false;
		if (Double.doubleToLongBits(length) != Double
				.doubleToLongBits(other.length))
			return false;
		if (Double.doubleToLongBits(xStart) != Double
				.doubleToLongBits(other.xStart))
			return false;
		if (Double.doubleToLongBits(yStart) != Double
				.doubleToLongBits(other.yStart))
			return false;
		return true;
	}
}
