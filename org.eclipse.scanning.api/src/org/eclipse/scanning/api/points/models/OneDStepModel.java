package org.eclipse.scanning.api.points.models;

public class OneDStepModel  extends BoundingBoxModel{

	private double step = 1;

	public double getStep() {
		return step;
	}

	public void setStep(double step) {
		this.step = step;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
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
		if (Double.doubleToLongBits(step) != Double
				.doubleToLongBits(other.step))
			return false;
		return true;
	}

}
