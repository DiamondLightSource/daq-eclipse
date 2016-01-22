package org.eclipse.scanning.api.points.models;

import java.beans.PropertyChangeListener;

public interface IBoundingLineModel {

	public BoundingLine getBoundingLine();

	public void setBoundingLine(BoundingLine boundingLine);

	public int hashCode();

	public boolean equals(Object obj);

	public void addPropertyChangeListener(PropertyChangeListener listener);

	public void removePropertyChangeListener(PropertyChangeListener listener);

}