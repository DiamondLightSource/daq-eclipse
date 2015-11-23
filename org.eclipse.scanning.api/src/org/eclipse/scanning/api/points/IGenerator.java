package org.eclipse.scanning.api.points;

import java.util.Iterator;
import java.util.List;


/**
 * Generator for a give type, T (for instance LissajousModel).
 * 
 * @author Matthew Gerring
 *
 * @param <T>
 */
public interface IGenerator<T> extends Iterable<Point> {
	
	/**
	 * The model for the generator.
	 * @return
	 */
	T getModel();
	void setModel(T model) throws GeneratorException;
	
	/**
	 * The class which contains points, may be null.
	 * @return
	 */
	<R> IPointContainer<R> getContainer();
	<R> void setContainer(IPointContainer<R> container) throws GeneratorException;

	/**
	 * The size of the points iterator. This call will be as fast as possible
	 * but can be as slow as iterating all points.
	 * @return
	 */
	int size() throws GeneratorException;
	
	/**
	 * Iterator over the points, fast because does not evaluate
	 * all points straight away, does it on the fly.
	 * 
	 * @return
	 */
	Iterator<Point> iterator();
	
	/**
	 * Relatively slow because all the points have to exist in memory.
	 * Points are lightweight and it is normally ok to have them all in memory.
	 * However if it can be avoided for a given scan, the scan will start faster.
	 * 
	 * @return
	 */
	List<Point> createPoints() throws GeneratorException;
}
