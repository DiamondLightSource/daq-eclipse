package org.eclipse.scanning.api.points;

import java.util.List;

/**
 * 
 * A position is a location of scannable values used in a scan.
 * The position might be X and Y or Temperature or all three.
 * 
 * @author Matthew Gerring
 *
 * @param <T>
 */
public interface IPosition {
	
	/**
	 * The number of named scalars in this position
	 * @return
	 */
	int size();

	/**
	 * The names of the scalars set for this position.
	 * For instance 'x' and 'y' for a map or 'Temperature'
	 * 
	 * @return
	 */
	List<String> getNames();
	
	/**
	 * The value of a named position. For instance get("X") to return the value of the 
	 * X IScannable double.
	 * 
	 * @param name
	 * @return
	 */
	Object get(String name);
	
	/**
	 * Creates a composite position with the values of this position 
	 * and the values of the passed in position.
	 * 
	 * @param other
	 * @return
	 */
	IPosition composite(IPosition other);
}
