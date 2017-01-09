package org.eclipse.scanning.api.stashing;

import java.io.File;

/**
 * Service for creating stashes which save objects to json
 * using the IMarshalling Service.
 * 
 * @author Matthew Gerring
 *
 */
public interface IStashingService {

	/**
	 * Create a stash using a path contructed from the user's home.
	 * The file = System.getProperty("user.home")+"/.solstice/"+fileName
	 * 
	 * @param path
	 * @return
	 */
	IStashing createStash(String fileName);
	
	/**
	 * Create a stash using a file
	 * @param path
	 * @return
	 */
	IStashing createStash(File file);
}
