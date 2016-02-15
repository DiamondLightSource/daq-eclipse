package org.eclipse.scanning.api;

import org.eclipse.scanning.api.points.IPosition;

/**
 * 
 * Something that can take part in a sequenced scan which can have its position set.
 * 
 * Unlike a detector, a scannable does not have a model and has attributes directly 
 * on the interface
 * 
 * @author Matthew Gerring
 *
 */
public interface IScannable<T> extends ILevel, INameable, IConfigurable<ScannableModel> {
	
	/**
	 * Returns the current position of the Scannable. Called by ConcurentScan at the end of the point. 
	 * 
	 * @return Current position with an element for each input and extra field. null if their are no fields.
	 * @throws DeviceException
	 */
	public T getPosition() throws Exception;
	
	/**
	 * Moves to the position required, blocking until it is complete.
	 * Similar to moveTo(...) in GDA8
	 * 
	 * Same as calling setPosition(value, null);
	 * 
	 * @param position
	 * @throws Exception
	 */
	public void setPosition(T value) throws Exception;

	
	/**
	 * Moves to the position required, blocking until it is complete.
	 * Similar to moveTo(...) in GDA8
	 * 
	 * @param value that this scalar should take.
	 * @param position if within in a scan or null if not within a scan.
	 * @throws Exception
	 */
	public void setPosition(T value, IPosition position) throws Exception;

	/**
	 * The unit is the unit in which the setPosition and getPosition values are in.
	 * @return String representation of unit which setPosition and getPosition are using.
	 */
	default String getUnit() {
		return null;
	}

}
