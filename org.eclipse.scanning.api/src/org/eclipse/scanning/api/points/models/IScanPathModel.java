package org.eclipse.scanning.api.points.models;

import java.beans.PropertyChangeListener;

public interface IScanPathModel {

	/**
	 * A 'friendly' name for display to the user
	 *
	 * @return the name of this path
	 */
	public String getName();

	public int hashCode();

	public boolean equals(Object obj);

	public void addPropertyChangeListener(PropertyChangeListener listener);

	public void removePropertyChangeListener(PropertyChangeListener listener);
}