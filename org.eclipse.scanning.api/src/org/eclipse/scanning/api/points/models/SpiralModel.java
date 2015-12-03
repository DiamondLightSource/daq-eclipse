package org.eclipse.scanning.api.points.models;

public class SpiralModel implements IBoundingBoxModel {

	private IBoundingBoxModel boundingBox = new BoundingBoxModel();
	private double scale = 1;

	public double getScale() {
		return scale;
	}
	public void setScale(double scale) {
		this.scale = scale;
	}
	@Override
	public double getWidth() {
		return boundingBox.getWidth();
	}
	@Override
	public void setWidth(double width) {
		boundingBox.setWidth(width);
	}
	@Override
	public double getHeight() {
		return boundingBox.getHeight();
	}
	@Override
	public void setHeight(double height) {
		boundingBox.setHeight(height);
	}
	@Override
	public boolean isParentRectangle() {
		return boundingBox.isParentRectangle();
	}
	@Override
	public void setParentRectangle(boolean isParentRectangle) {
		boundingBox.setParentRectangle(isParentRectangle);
	}
	@Override
	public double getxStart() {
		return boundingBox.getxStart();
	}
	@Override
	public void setxStart(double xStart) {
		boundingBox.setxStart(xStart);
	}
	@Override
	public double getyStart() {
		return boundingBox.getyStart();
	}
	@Override
	public void setyStart(double yStart) {
		boundingBox.setyStart(yStart);
	}
	@Override
	public double getAngle() {
		return boundingBox.getAngle();
	}
	@Override
	public void setAngle(double angle) {
		boundingBox.setAngle(angle);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((boundingBox == null) ? 0 : boundingBox.hashCode());
		long temp;
		temp = Double.doubleToLongBits(scale);
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
		SpiralModel other = (SpiralModel) obj;
		if (boundingBox == null) {
			if (other.boundingBox != null)
				return false;
		} else if (!boundingBox.equals(other.boundingBox))
			return false;
		if (Double.doubleToLongBits(scale) != Double
				.doubleToLongBits(other.scale))
			return false;
		return true;
	}
}
