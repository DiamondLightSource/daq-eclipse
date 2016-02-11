package org.eclipse.scanning.api.points.models;

import java.beans.PropertyChangeListener;

public interface IScanPathModel {

	/**
	 * A 'friendly' name for display to the user
	 *
	 * @return the name of this path
	 */
	public String getName();
	
	/**
	 * A non-user interface value for the name of this model.
	 */
	public String getUniqueKey();
	
	/**
	 * A non-user interface value for the name of this model.
	 */
	public void setUniqueKey(String key);

	/**
	 * Property change support
	 * @param listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener);

	/**
	 * Property change support
	 * @param listener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener);
}