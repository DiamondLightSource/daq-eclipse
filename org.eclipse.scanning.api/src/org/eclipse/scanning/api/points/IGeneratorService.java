package org.eclipse.scanning.api.points;


/**
 * This service generates points for a given scan type.
 * 
 * <p>
 * 
 * <usage><code>
 * IGeneratorService pservice  = ... // OSGi <br>
 * IGenerator<Point> generator = pservice.createGenerator(...); <br>
 * Iterator<Point>   it        = generator.iterator(); <br>
 * ... // Use iterator in a scan. <br>
 *  <br>
 * int size = generator.size(); // Use size to tell user in GUI the whole size. <br>
 * List<Point> allPoints = generator.createPoints(); // Create and return all the points in memory (might be large). <br>
 * 
 * </code></usage>
 * 
 * @author Matthew Gerring
 *
 */
public interface IGeneratorService {

	<T> IGenerator<T> createGenerator(ScanType type);
}
