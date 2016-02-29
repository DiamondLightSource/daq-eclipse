package org.eclipse.scanning.api.points.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * A model for a scan along a straight line in two-dimensional space, starting at the beginning of the line and moving
 * in steps of the size given in this model.
 *
 * @author Colin Palmer
 *
 */
public class OneDStepModel extends AbstractPointsModel implements IBoundingLineModel {

	protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}
	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	private BoundingLine boundingLine;
	private double step = 1;

	@Override
	public String getName() {
		return "Step";
	}
	public double getStep() {
		return step;
	}
	public void setStep(double step) {
		double oldValue = this.step;
		this.step = step;
		this.pcs.firePropertyChange("step", oldValue, step);
	}
	@Override
	public BoundingLine getBoundingLine() {
		return boundingLine;
	}
	@Override
	public void setBoundingLine(BoundingLine boundingLine) {
		BoundingLine oldValue = this.boundingLine;
		this.boundingLine = boundingLine;
		this.pcs.firePropertyChange("boundingLine", oldValue, boundingLine);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((boundingLine == null) ? 0 : boundingLine.hashCode());
		long temp;
		temp = Double.doubleToLongBits(step);
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
		OneDStepModel other = (OneDStepModel) obj;
		if (boundingLine == null) {
			if (other.boundingLine != null)
				return false;
		} else if (!boundingLine.equals(other.boundingLine))
			return false;
		if (Double.doubleToLongBits(step) != Double
				.doubleToLongBits(other.step))
			return false;
		return true;
	}
}
