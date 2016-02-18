package org.eclipse.scanning.api.scan.process;

/**
 * 
 * This service preprocesses a scan request made by the user interface.
 * 
 * It reorders the scan if a specific device is used, for instance if
 * a grid scan defined in the user interface but the scanning implementation
 * only has a line scan, the grid model will be removed and an appropriate 
 * StepModel created to change the points of the CPU scan.
 * 
 * A client may set the ignorePreprocess flag on ScanRequest if it knows that
 * what is submitted should be the exact thing run. This bypasses the scan pre-processing.
 * 
 * Preprocessors should be added by extenion point. Each name used is considered unqiue. If
 * more than one preprocessor is defined with the same name, the last one read by the extension
 * point system will be than put in the map of processors.
 * 
 * @author Matthew Gerring
 *
 */
public interface IPreprocessingService {

	/**
	 * Gets a preprocessor and returns it or null if no preprocessor can be found for this name.
	 * 
	 * NOTE One preprocessor exists for a give name and service and will used multiple times.
	 * It is not permissible to store local data in the preprocessor which would interfere with
	 * multiple calls to preprocess from different threads.
	 * 
	 * @param name The name of the preprocessor
	 * @param req
	 * @return
	 */
	IPreprocessor getPreprocessor(String name);
}
