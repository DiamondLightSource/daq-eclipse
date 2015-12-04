package org.eclipse.scanning.api.points.models;

/**
 * <pre>
 * snake = true
 * -------------------->
 *                     |
 * <--------------------
 * |
 * --------------------> etc.
 * 
 * snake = false
 * -------------------->
 * -------------------->
 * -------------------->
 * </pre>
**/
public class RasterModel implements IModelWithBoundingBox {

	private BoundingBox boundingBox;
	private double xStep = 1;
	private double yStep = 1;
	private boolean snake = false;
	public double getxStep() {
		return xStep;
	}
	public void setxStep(double xStep) {
		this.xStep = xStep;
	}
	public double getyStep() {
		return yStep;
	}
	public void setyStep(double yStep) {
		this.yStep = yStep;
	}
	public boolean isSnake() {
		return snake;
	}
	public void setSnake(boolean biDirectional) {
		this.snake = biDirectional;
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
		int result = 1;
		result = prime * result
				+ ((boundingBox == null) ? 0 : boundingBox.hashCode());
		result = prime * result + (snake ? 1231 : 1237);
		long temp;
		temp = Double.doubleToLongBits(xStep);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yStep);
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
		RasterModel other = (RasterModel) obj;
		if (boundingBox == null) {
			if (other.boundingBox != null)
				return false;
		} else if (!boundingBox.equals(other.boundingBox))
			return false;
		if (snake != other.snake)
			return false;
		if (Double.doubleToLongBits(xStep) != Double
				.doubleToLongBits(other.xStep))
			return false;
		if (Double.doubleToLongBits(yStep) != Double
				.doubleToLongBits(other.yStep))
			return false;
		return true;
	}
}
