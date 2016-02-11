package org.eclipse.scanning.api.points;

import java.util.Collection;

/**
 * This service generates points for a given scan type.
 * 
 * <p>
 * 
 * <usage><code>
 * IPointGeneratorService pservice  = ... // OSGi <br>
 * 
 * LissajousModel model = new LissajousModel();<br>
 *  ... // Set values
 * 
 * IPointGenerator<LissajousModel,Point> generator = pservice.createGenerator(model, roi); <br>
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
public interface IPointGeneratorService {

	/**
	 * Used to create a point generator of a given type
	 * @param model
	 * @param region, a reference to an IROI for instance, maybe <b>null</b> if no IROI exists for this scan.
	 * @return
	 */
	<T,R,P> IPointGenerator<T,P> createGenerator(T model, R... roi) throws GeneratorException;

	/**
	 * Create a nested or compound generator.
	 * Each generator in the varargs argument is another level to the loop.
	 * 
	 * @param generators
	 * @return
	 * @throws GeneratorException
	 */
	IPointGenerator<?,IPosition> createCompoundGenerator(IPointGenerator<?,? extends IPosition>... generators) throws GeneratorException;

	/**
	 * Each IPointGenerator must have a unique id which is used to refer to it in the user interface.
	 * @return
	 */
	Collection<String> getRegisteredGenerators();
	
	/**
	 * Creates a generator by id which has no model associated with it.
	 * @param id
	 * @return
	 */
	<T,P> IPointGenerator<T,P> createGenerator(String id) throws GeneratorException;
}
