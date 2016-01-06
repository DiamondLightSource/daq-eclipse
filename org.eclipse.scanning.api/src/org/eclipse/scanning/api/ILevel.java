package org.eclipse.scanning.api;

/**
 * Interface for any device with a level. Level is used for instance in scanning to 
 * define the order that devices are moved to in the scan.
 * 
 * @author Matthew Gerring
 *
 */
public interface ILevel extends INameable {

	/**
	 * Used for ordering the operations of Scannables during scans
	 * 
	 * @param level
	 */
	public void setLevel(int level);

	/**
	 * get the operation level of this scannable.
	 * 
	 * @return int - the level
	 */
	public int getLevel();


}
