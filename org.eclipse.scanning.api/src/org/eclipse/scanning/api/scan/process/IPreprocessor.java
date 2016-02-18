package org.eclipse.scanning.api.scan.process;

import org.eclipse.scanning.api.event.scan.ScanRequest;

/**
 * 
<pre>
<b>pre·proc·es·sor</b>
prēˈpräsˌesər,-ˈprōsˌesər,-əsər/
<i>noun</i>
A computer program that modifies data to conform with the input requirements of another program.
</pre>
<br>


 * A preprocessor contributed by the extension point
 * org.eclipse.scanning.api.preprocessor which makes a 
 * ScanRequest into one which matches the available hardware
 * for a given beamline.
 * 
 * {@link IPreprocessingService}
 * 
 * @author Matthew Gerring
 *
 */
public interface IPreprocessor {

	/**
	 * Preprocessor name. For instance "I18", "I05-1" or "mapping".
	 * The actual name is provided by the extension point and should 
	 * match the configuration of the process which is running scanning.
	 * 
	 * For instance if the config name is "I18" the preprocessor wil;
	 * 
	 * @return
	 */
	String getName();
	
	/**
	 * Call to run the preprocessor, read the available hardware and construct a legal
	 * ScanRequest
	 * 
	 * @param req The request sent by the user interface
	 * @return The processed request. The processor might modify the ScanRequest in place and return it or return an entirely new request.
	 */
	<T> ScanRequest<T> preprocess(ScanRequest<T> req) throws ProcessingException;
}
