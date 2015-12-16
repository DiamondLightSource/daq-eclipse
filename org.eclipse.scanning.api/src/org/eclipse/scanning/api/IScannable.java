package org.eclipse.scanning.api;

/**
 * 
 * Something that can take part in a sequenced scan which can have its position set.
 * 
 * Unlike a detector, a scannable does not have a model and has attributes directly 
 * on the interface
 * 
 * @author fcp94556
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
	 * @param position
	 * @throws Exception
	 */
	public void setPosition(T position) throws Exception;

}
