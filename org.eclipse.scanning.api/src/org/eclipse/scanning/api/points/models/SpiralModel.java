package org.eclipse.scanning.api.points.models;

public class SpiralModel implements IModelWithBoundingBox {

	private BoundingBox boundingBox;
	private double scale = 1;

	public double getScale() {
		return scale;
	}
	public void setScale(double scale) {
		this.scale = scale;
	}
	@Override
	public BoundingBox getBoundingBox() {
		return boundingBox;
	}
	@Override
	public void setBoundingBox(BoundingBox boundingBox) {
		this.boundingBox = boundingBox;
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
