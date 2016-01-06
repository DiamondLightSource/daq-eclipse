package org.eclipse.scanning.api;

/**
 * A nameable is any device with a name.
 * 
 * @author Matthew Gerring
 *
 */
public interface INameable {

	/**
	 * Name of the scannable
	 * @return
	 */
	public String getName();
	
	/**
	 * Set the name of the device.
	 * @param name
	 */
	public void setName(String name);

}
