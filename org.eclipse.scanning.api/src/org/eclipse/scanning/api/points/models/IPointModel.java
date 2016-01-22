package org.eclipse.scanning.api.points.models;

import java.beans.PropertyChangeListener;

public interface IPointModel {

	public double getX();

	public void setX(double x);

	public double getY();

	public void setY(double y);

	public int hashCode();

	public boolean equals(Object obj);

	public void addPropertyChangeListener(PropertyChangeListener listener);

	public void removePropertyChangeListener(PropertyChangeListener listener);

}