package org.eclipse.scanning.api.points;

import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.device.IDeviceService;

/**
 * A position is a location of one or more values. It is
 * used to group scannables when moving to a position
 * by level.
 * <p>
 * For instance a group of scannables which need to be at a
 * certain location. For example at the start of a scan or
 * for a scan datapoint.
 * <p>
 * <B>NOTE:</B> An object is available using IDeviceService.createPositioner()
 * called IPositioner which can move scannables to a given
 * position by level.
 * 
 * @see IDeviceService
 * @see org.eclipse.scanning.api.scan.event.IPositioner
 *
 * @author Matthew Gerring
 *
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
	 * Get the index of the data for instance in a scan of temperature from 290 to 300 step 1,
	 * the indices will be 0-10.
	 * 
	 * @return
	 */
	int getIndex(String name); 
	
	/**
	 * Creates a composite position with the values of this position 
	 * and the values of the passed in position.
	 * 
	 * This position's values take precedence if the names conflict.
	 * This is like an implementation of Map where the putAll does not
	 * overwrite.
	 * 
	 * @param other
	 * @return
	 */
	IPosition composite(IPosition other);
	
	/**
	 * The step where the position was in a scan, if it is a position being
	 * generated from a scan. If not the value will be -1
	 * @return
	 */
	int getStepIndex();
	
	/**
	 * The step where the position was in a scan, if it is a position being
	 * generated from a scan. If not the value will be -1
	 * @param step
	 */
	void setStepIndex(int step);

	/**
	 * 
	 * @return the data indices mapped name:index
	 */
	Map<String, Integer> getIndices();
}
