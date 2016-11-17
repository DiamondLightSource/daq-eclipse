package org.eclipse.scanning.api.device.models;

import java.util.List;

/**
 * Model for a malcolm device.
 * TODO: do we need the name as well, if so extend INameable
 */
public interface IMalcolmModel {
	
	/**
	 * Get the directory where malcolm will write its h5 files to. The directory should exist at
	 * the point that the malcolm device is configured, malcolm is not responsible for creating it.
	 * @return path of malcolm output directory
	 */
	public String getFileDir();
	
	/**
	 * Get the names of the scan axes that are controlled by malcolm.
	 * @return axes to move
	 */
	public List<String> getAxesToMove();

}
