/*-
 * Copyright © 2016 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package org.eclipse.scanning.api.ui;

import java.beans.PropertyChangeListener;

/**
 * Interface to allow the current configuration of a mapping scan
 * (or how it is displayed in the UI) to be determined
 * 
 */
public interface IStageScanConfiguration {

	/**
	 * Return the name of the fast scan axis
	 * @return fastAxisName
	 */
	String getActiveFastScanAxis();

	/**
	 * Return the name of the fast scan axis
	 * @return slowAxisName
	 */
	String getActiveSlowScanAxis();
	
	/**
	 * Returns the name of the associated axis. This may be the z-axis, for example.
	 * @return
	 */
	String getAssociatedAxis();
	
	/**
	 * Add a property change listener
	 * 
	 * @param listener
	 */
	void addPropertyChangeListener(PropertyChangeListener listener);

	/**
	 * Remove a property change listener
	 * 
	 * @param listener
	 */
	void removePropertyChangeListener(PropertyChangeListener listener);


	

}