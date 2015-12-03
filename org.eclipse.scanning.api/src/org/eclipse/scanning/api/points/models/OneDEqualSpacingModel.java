package org.eclipse.scanning.api.points.models;

public class OneDEqualSpacingModel implements ILinearModel {

	private LinearModel line = new LinearModel();
	private int points;

	public int getPoints() {
		return points;
	}
	public void setPoints(int points) {
		this.points = points;
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
		result = prime * result + points;
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
		OneDEqualSpacingModel other = (OneDEqualSpacingModel) obj;
		if (line == null) {
			if (other.line != null)
				return false;
		} else if (!line.equals(other.line))
			return false;
		if (points != other.points)
			return false;
		return true;
	}
}
