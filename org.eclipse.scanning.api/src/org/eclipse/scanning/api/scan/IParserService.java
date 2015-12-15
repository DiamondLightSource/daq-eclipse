package org.eclipse.scanning.api.scan;

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
 * IScanner scanner = sservice.createScanner();<br>
 * scanner.configure(...);<br>
 * scanner.run();<br>
 * 
 * </code></usage>
 * 
 * @see http://confluence.diamond.ac.uk/pages/viewpage.action?spaceKey=MAP&title=discussion+item%3A+generality+of+continuous+scans
 * @author Matthew Gerring
 *
 */
public interface IParserService {

	/**
	 * Create a parser for a give scan, optionally parametixing the
	 * model returned. Some scans will consist of multiple models,
	 * for instance a Compound generator scan like "scan dcm 1 5 1 spiral(loops=720,expand%=10,rot=ccw,start=perip) detector 0.1"
	 * will generate different models for dcm and spiral. In this case the
	 * model for the position generation is undefined. Therefore the parser would simply be:
	 * IParser<?> parser = pservice.createParser("scan dcm 1 5 1 spiral(loops=720,expand%=10,rot=ccw,start=perip) detector 0.1");
	 * Object xmodel = parser.getModel("dcm");
	 * Object smodel = parser.getModel("spiral");
	 * 
	 * @param scan
	 * @return
	 */
	<T> IParser<T> createParser(String scan) throws ParsingException;
}
