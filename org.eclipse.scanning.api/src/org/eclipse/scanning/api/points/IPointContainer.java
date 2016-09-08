package org.eclipse.scanning.api.points;

/**
 * Interface to check if a given point is contained
 * 
 * IPointContainer.containsPoint(...) != IROI.containsPoint(...) because
 * the IROI is in the data coordinates and the IPointContainer is in the 
 * motor coordinates.
 * 
 * @author Matthew Gerring
 *
 */
public interface IPointContainer {

	/**
	 * Check a given point is contained by the implementor of this interface.
	 * @return
	 */
	public boolean containsPoint(IPosition point);
	
}
