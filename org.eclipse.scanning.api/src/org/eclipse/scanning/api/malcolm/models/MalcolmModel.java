package org.eclipse.scanning.api.malcolm.models;

/**
 * 
 * A model which all malcolm models should conform to. Currently this is not enforced
 * because we need to have further debate about models exposed to Java for malcolm project v2.
 * 
 * When it is enforced the type on IMalcolmDevice will have to be implementing MalcolmModel
 * 
 * @author Matthew Gerring
 *
 */
public interface MalcolmModel {

	/**
	 * The path to the h5 file which the malcolm device writes.
	 * Normally this will be the SWMR file which the device is
	 * writing efficiently and allowing a simultaneous read of.
	 * 
	 * @return
	 */
	String getFilePath();
	void setFilePath(String filePath);
	
}
