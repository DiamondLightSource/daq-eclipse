package org.eclipse.scanning.api.points.models;

import java.beans.PropertyChangeListener;

public interface IBoundingBoxModel {

	public BoundingBox getBoundingBox();

	public void setBoundingBox(BoundingBox boundingBox);

	public int hashCode();

	public boolean equals(Object obj);

	public void addPropertyChangeListener(PropertyChangeListener listener);

	public void removePropertyChangeListener(PropertyChangeListener listener);

}