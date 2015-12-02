package org.eclipse.scanning.api.points.models;

public class RasterModel implements IBoundingBoxModel {

	private IBoundingBoxModel boundingBox = new BoundingBoxModel();
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
