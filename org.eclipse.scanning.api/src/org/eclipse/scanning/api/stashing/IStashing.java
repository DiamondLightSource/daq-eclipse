package org.eclipse.scanning.api.stashing;

import java.io.File;

public interface IStashing {

	/**
	 * 
	 * @return true if the file exists and has something in it.
	 */
	boolean isStashed();

	/**
	 * Save the object to our stash file, replacing anything else in the file.
	 * @param object
	 * @throws Exception
	 */
	<T> void stash(T object) throws Exception;
	
	/**
	 * Unstash and return the file.
	 * @param clazz
	 * @return null if file not there
	 * @throws Exception if file non-empty but cannot be unstashed to T
	 */
	<T> T unstash(Class<T> clazz) throws Exception;

	/**
	 * Save the object to the stash showing a dialog to the user
	 * if it exists and they are overwriting.
	 * @param object
	 */
	<T> void save(T object);
	
	/**
	 * Load the object from the stash, showing a dialog to the user if the
	 * object cannot be retrived from the stash.
	 * 
	 * @param clazz
	 * @return
	 */
	<T> T load(Class<T> clazz);
	
	/**
	 * 
	 * @return the file to which we are stashing.
	 */
	File getFile();

}
