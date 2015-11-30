package org.eclipse.scanning.api.points;


/**
 * This service generates points for a given scan type.
 * 
 * <p>
 * 
 * <usage><code>
 * IGeneratorService pservice  = ... // OSGi <br>
 * 
 * LissajousModel model = new LissajousModel();<br>
 *  ... // Set values
 * 
 * IGenerator<LissajousModel,Point> generator = pservice.createGenerator(model, roi); <br>
 * 
 * Iterator<Point> it = generator.iterator(); <br>
 * ... // Use iterator in a scan. <br>
 * 
 *  <br>
 * // Use size to tell user in GUI the whole size. Avoids making all points if it can <br>
 * int size = generator.size();<br>
 * 
 * // Create and return all the points in memory (might be large). Avoid if possible <br>
 * List<Point> allPoints = generator.createPoints(); <br>
 * 
 * </code></usage>
 * 
 * @author Matthew Gerring
 *
 */
public interface IGeneratorService {

	/**
	 * Used to create a point generator of a given type
	 * @param model
	 * @param region, a reference to an IROI for instance, maybe <b>null</b> if no IROI exists for this scan.
	 * @return
	 */
	<T,R,P> IGenerator<T,P> createGenerator(T model, R... roi) throws GeneratorException;

	/**
	 * Create a nested or compound generator.
	 * Each generator in the varargs argument is another level to the loop.
	 * 
	 * @param generators
	 * @return
	 * @throws GeneratorException
	 */
	IGenerator<?,IPosition> createCompoundGenerator(IGenerator<?,? extends IPosition>... generators) throws GeneratorException;
}
