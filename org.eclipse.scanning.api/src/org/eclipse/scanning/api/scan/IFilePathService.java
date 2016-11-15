package org.eclipse.scanning.api.scan;


/**
 * This service provides useful file paths for writing to. It has method to get the
 * path of the next scan file.
 * 
 * @author Matthew Gerring
 *
 */
public interface IFilePathService {

	/**
	 * Determine and return the next file path to write to.
	 * @return next file path
	 * @param template, for instance the sample name, may be null or empty string if no template is required.
	 * @throws Exception if the next path cannot be calculated for any reason
	 */
	String getNextPath(String template) throws Exception;
	
	/**
	 * Creates and returns the path of a new folder in the same parent folder as file path with
	 * the same name as the given file, minus the file extension. This folder can be used to
	 * write linked files to for the given scan. If the filename given is a filepath, only the
	 * last segment will be used.
	 * @param filename the name of the file 
	 * @return path of new folder
	 * @throws Exception if the folder cannot be created for any reason
	 */
	String createFolderForLinkedFiles(String filename) throws Exception;
	
	/**
	 * Returns the name of the path returned by the most recent call to
	 * {@link #getNextPath()}. Note that this is not guaranteed to be the file for the current
	 * scan.
	 * @return last file path
	 * @throws IllegalStateException if {@link #getNextPath()} has never been called
	 */
	String getMostRecentPath() throws IllegalStateException;
	
	/**
	 * Returns the location of the directory to use for temporary files
	 * @return location of temp directory
	 */
	String getTempDir();
	
	/**
	 * Returns the location of the directory in which to place processed files.
	 * @return processed files
	 */
	String getProcessedFilesDir();
	
}
