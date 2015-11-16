package org.eclipse.scanning.api.points;

/**
 * This interface is used to wrap an IROI.containsPoint(...) call
 * and is intended to ensure that no dependency on scanning of dawn regions exists.
 * 
 * @author Matthew Gerring
 *
 */
public interface IPointContainer {

	/**
	 * Check a given point is contained by the implementor of this interface.
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean containsPoint(double x, double y);
}
