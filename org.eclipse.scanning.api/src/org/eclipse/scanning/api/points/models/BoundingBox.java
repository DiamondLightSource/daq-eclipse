package org.eclipse.scanning.api.points.models;

/**
 * A model defining a box in two dimensional space, which can be used to confine and give scale to a {@link IBoundingBoxModel}.
 *
 * @author Colin Palmer
 *
 */
public class BoundingBox {

	public BoundingBox() {
	}
	public BoundingBox(double xStart, double yStart, double width, double height) {
		super();
		this.xStart = xStart;
		this.yStart = yStart;
		this.width = width;
		this.height = height;
	}
	private double xStart;
	private double yStart;
	private double width;
	private double height;

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
	public double getWidth() {
		return width;
	}
	public void setWidth(double width) {
		this.width = width;
	}
	public double getHeight() {
		return height;
	}
	public void setHeight(double height) {
		this.height = height;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(height);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(width);
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
		BoundingBox other = (BoundingBox) obj;
		if (Double.doubleToLongBits(height) != Double
				.doubleToLongBits(other.height))
			return false;
		if (Double.doubleToLongBits(width) != Double
				.doubleToLongBits(other.width))
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
