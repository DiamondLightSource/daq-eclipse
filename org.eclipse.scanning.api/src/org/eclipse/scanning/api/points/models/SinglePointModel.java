package org.eclipse.scanning.api.points.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * A model for a scan at a single two-dimensional point.
 *
 * @author Colin Palmer
 *
 */
public class SinglePointModel extends AbstractPointsModel implements IPointModel {

	protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}
	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	private double x;
	private double y;

	@Override
	public String getName() {
		return "Single Point";
	}
	@Override
	public double getX() {
		return x;
	}
	@Override
	public void setX(double x) {
		double oldValue = this.x;
		this.x = x;
		this.pcs.firePropertyChange("x", oldValue, x);
	}
	@Override
	public double getY() {
		return y;
	}
	@Override
	public void setY(double y) {
		double oldValue = this.y;
		this.y = y;
		this.pcs.firePropertyChange("y", oldValue, y);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
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
		SinglePointModel other = (SinglePointModel) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}
}
