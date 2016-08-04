package org.eclipse.scanning.api.points.models;

import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * A model which defines the movement parameters for one or more dimensions of a scan.
 *
 * @author Colin Palmer
 *
 */
public interface IScanPathModel {

    /**
     * The names of the axes which will be scanned by this model.
     * @return
     */
	List<String> getScannableNames();

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