package org.eclipse.scanning.api.points.models;

public class RasterModel  extends RectangularModel{

	private double xStep = 1;
	private double yStep = 1;
	private boolean biDirectional = false;
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
	public boolean isBiDirectional() {
		return biDirectional;
	}
	public void setBiDirectional(boolean biDirectional) {
		this.biDirectional = biDirectional;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (biDirectional ? 1231 : 1237);
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
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RasterModel other = (RasterModel) obj;
		if (biDirectional != other.biDirectional)
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
