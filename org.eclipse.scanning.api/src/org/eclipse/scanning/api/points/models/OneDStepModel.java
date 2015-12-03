package org.eclipse.scanning.api.points.models;

public class OneDStepModel implements ILinearModel {

	private LinearModel line = new LinearModel();
	private double step = 1;

	public double getStep() {
		return step;
	}
	public void setStep(double step) {
		this.step = step;
	}
	@Override
	public double getLength() {
		return line.getLength();
	}
	@Override
	public void setLength(double length) {
		line.setLength(length);
	}
	@Override
	public double getxStart() {
		return line.getxStart();
	}
	@Override
	public void setxStart(double minX) {
		line.setxStart(minX);
	}
	@Override
	public double getyStart() {
		return line.getyStart();
	}
	@Override
	public void setyStart(double minY) {
		line.setyStart(minY);
	}
	@Override
	public double getAngle() {
		return line.getAngle();
	}
	@Override
	public void setAngle(double angle) {
		line.setAngle(angle);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((line == null) ? 0 : line.hashCode());
		long temp;
		temp = Double.doubleToLongBits(step);
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
		OneDStepModel other = (OneDStepModel) obj;
		if (line == null) {
			if (other.line != null)
				return false;
		} else if (!line.equals(other.line))
			return false;
		if (Double.doubleToLongBits(step) != Double
				.doubleToLongBits(other.step))
			return false;
		return true;
	}
}
