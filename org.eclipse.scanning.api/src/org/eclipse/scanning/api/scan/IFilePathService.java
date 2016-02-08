package org.eclipse.scanning.api.scan;


/**
 * This service provides the path to the next scan file which
 * the scanning will write to. It should be implemented to return
 * the next path.
 * 
 * @author Matthew Gerring
 *
 */
public interface IFilePathService {

	/**
	 * Determine and return the next file path to
	 * write to
	 * @return
	 */
	String nextPath() throws Exception;
}
