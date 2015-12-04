package org.eclipse.scanning.api.scan;

import org.eclipse.scanning.api.points.IPosition;


/**
 * 
 * Anatomy of a CPU scan (non-malcolm)
 * 
 *  <br>
 *&nbsp;_________<br>
 *_|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|________  collectData() Tell detector to collect<br>
 *&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;_________<br>
 *_________|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|_  readout() Tell detector to readout<br>
 *&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;_______<br>
 *_________|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|___  moveTo()  Scannables move motors to new position<br>
 * <br>
 *<br>
 * A MalcolmDevice is also an IScanner which may operate with an arbitrary model, usually driving hardware.<br>
 * <br>
 * <usage><code>
 * IParserService pservice = ...// OSGi<br>
 * <br>
 * // Parse the scan command, throws an exception<br>
 * IParser<StepModel> parser = pservice.createParser(...)<br>
 * // e.g. "scan x 0 5 0.1 analyser"<br>
 * <br>
 * // Now use the parser to create a generator<br>
 * IGeneratorService gservice = ...// OSGi<br>
 * StepModel model = parser.getModel("x");<br>
 * Iterable<IPosition>    gen = gservice.createGenerator(model)<br>
 * <br>
 * // Now scan the point iterator<br>
 * IScanningService sservice = ...// OSGi<br>
 * IScanner<ScanModel> scanner = sservice.createScanner(...);
 * scanner.configure(model);
 * scanner.run();
 * 
 * </code></usage>
 *
 * @author Matthew Gerring
 *
 *
 */
public interface IScanningService {
	
	/**
	 * This method sets the value of the scannables named to this position.
	 * It takes into account the levels of the scannbles. 
	 * It is blocking until all the scannables have reached the desired location.
	 * 
	 * @return
	 * @throws ScanningException
	 */
	IPositioner createPositioner() throws ScanningException;

	/**
	 * This method sets the value of the scannables named to this position.
	 * It takes into account the levels of the scannbles. 
	 * It is blocking until all the scannables have reached the desired location.
	 * 
	 * @return
	 * @throws ScanningException
	 */
	IPositioner createPositioner(IHardwareConnectorService hservice) throws ScanningException;

	/**
	 * Create an empty scanner which can run an iterable to complete a scan.
	 * 
	 * The model is provided and the configure(...) method called on the scanner 
	 * automatically. A ScanningException is thrown if the model is invalid.
	 * 
	 * @param model, information to do the scan
	 * @return
	 * @throws ScanningException
	 */
	<T> IScanner<T> createScanner(T model) throws ScanningException;
	
	/**
	 * Create an empty scanner which can run an iterable to complete a scan.
	 * 
	 * The model is provided and the configure(...) method called on the scanner 
	 * automatically. A ScanningException is thrown if the model is invalid.
	 * 
	 * @param model, information to do the scan
	 * @param hservice, may be null, in which case system looks for service using OSGi
	 * @return
	 * @throws ScanningException
	 */
	<T> IScanner<T> createScanner(T model, IHardwareConnectorService hservice) throws ScanningException;
	
}
