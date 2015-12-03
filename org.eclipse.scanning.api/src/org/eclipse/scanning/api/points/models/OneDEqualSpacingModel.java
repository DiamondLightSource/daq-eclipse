package org.eclipse.scanning.api.points.models;

public class OneDEqualSpacingModel implements IModelWithBoundingLine {

	private BoundingLine boundingLine;
	private int points;

	public int getPoints() {
		return points;
	}
	public void setPoints(int points) {
		this.points = points;
	}
	@Override
	public BoundingLine getBoundingLine() {
		return boundingLine;
	}
	@Override
	public void setBoundingLine(BoundingLine boundingLine) {
		this.boundingLine = boundingLine;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((boundingLine == null) ? 0 : boundingLine.hashCode());
		result = prime * result + points;
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
		OneDEqualSpacingModel other = (OneDEqualSpacingModel) obj;
		if (boundingLine == null) {
			if (other.boundingLine != null)
				return false;
		} else if (!boundingLine.equals(other.boundingLine))
			return false;
		if (points != other.points)
			return false;
		return true;
	}
}
