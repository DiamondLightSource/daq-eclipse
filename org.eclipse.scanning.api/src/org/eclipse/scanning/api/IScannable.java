package org.eclipse.scanning.api;

/**
 * 
 * Something that can take part in a sequenced scan which can have its position set.
 * 
 * @author fcp94556
 *
 */
public interface IScannable<T> extends ILevel, INameable {
	
	/**
	 * Returns the current position of the Scannable. Called by ConcurentScan at the end of the point. 
	 * 
	 * @return Current position with an element for each input and extra field. null if their are no fields.
	 * @throws DeviceException
	 */
	public T getPosition() throws Exception;
}
