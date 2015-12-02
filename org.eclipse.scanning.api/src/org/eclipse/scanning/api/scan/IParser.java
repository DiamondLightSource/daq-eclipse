package org.eclipse.scanning.api.scan;

import java.util.List;
import java.util.Map;

/**
 * Naive long winded usage of the parser API below to show how it should perform.<br>
 * However in fact the parser will be embedded as a subsystem of the scan.<br>
 * There is a utility for creating a compound points generator for<br>
 * any parser. However not putting this method directly on IParser<br>
 * means that the parser has a simple and well defined role.<br>
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
 * IGenerator<?>     gen = gservice.createGenerator(model)<br>
 * <br>
 * // Now scan the point iterator<br>
 * IScanningService sservice = ...// OSGi<br>
 * IScanner scanner = sservice.createScanner();
 * scanner.configure(gen, parser);
 * scanner.run();
 * 
 * </code></usage>
 * 
 * @see http://confluence.diamond.ac.uk/pages/viewpage.action?spaceKey=MAP&title=discussion+item%3A+generality+of+continuous+scans
 * @author Matthew Gerring
 *
 */
public interface IParser<T> {
	
	/**
	 * The original scan command which this parser is processing.
	 * @return
	 */
	String getCommand();
	
	/**
	 * Get the names of the scannables in order. For instance:
	 * Scan x 0 5 0.1 y 0 5 0.1 analyser
	 * would give:
	 * ["x","y"]
	 * 
	 * @return
	 */
	List<String> getScannableNames();

	/**
	 * Get the names of the detectors in order. For instance:
	 * Scan x 0 5 0.1 y 0 5 0.1 analyser 0.1 det 0.2
	 * would give:
	 * ["analyser","det"]
	 * 
	 * @return
	 */
	List<String> getDetectorNames();
	
	/**
	 * Get the exposures of the detectors, keys are ordered (LinkedHashMap). For instance:
	 * scan x 0 5 0.1 y 0 5 0.1 analyser 0.1 det 0.2
	 * would give the map:
	 * ["analyser":0.1,"det":0.2]
	 * 
	 * @return map of exposures
	 */
	Map<String, Number> getExposures();


	/**
	 * Get the point generation model for a given scannable
	 * For instance in the example:
	 * scan x 0 5 0.1 analyser
	 * Would gove getModel("x") StepModel with start stop and step set to  0, 5 and 0.1 respectively.
	 * 
	 * @param scannableName
	 * @return
	 */
	T getModel(String scannableName);
}
