package org.eclipse.scanning.api.points;

import java.util.Iterator;
import java.util.List;


/**
 * Generator for a give type, T (for instance LissajousModel).
 * 
 * The generator is an iterator used in the scan and a controller object 
 * for the user interface which provides naming information about the
 * type of scan.
 * 
 * @author Matthew Gerring
 *
 * @param <T>
 */
public interface IPointGenerator<T, P> extends Iterable<P> {
	
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
	List<IPointContainer<?>> getContainers();
	void setContainers(List<IPointContainer<?>> container) throws GeneratorException;

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
	Iterator<P> iterator();
	
	/**
	 * Relatively slow because all the points have to exist in memory.
	 * Points are lightweight and it is normally ok to have them all in memory.
	 * However if it can be avoided for a given scan, the scan will start faster.
	 * 
	 * @return
	 */
	List<P> createPoints() throws GeneratorException;
	
	/**
	 * The id for this generator. Generators defined by extension must set an it.
	 * Those defined by 
	 * 
	 * @return
	 */
	public String getId();
	
	/**
	 * The short label shown to the user for this generator.
	 * @return
	 */
	public String getLabel();
	
	/**
	 * The long description shown to the user for this generator.
	 */
	public String getDescription();
	
	/**
	 * 
	 * @return true if the user should be able to use this generator in the user interface.
	 */
	public boolean isVisible();
	
	/**
	 * 
	 * @return false if the user has disabled this generator from the compound scan but does not want to delete it.
	 */
	public boolean isEnabled();
	public void setEnabled(boolean enabled);
	
	/**
	 * The relative icon path to provide a custom icon for the generator.
	 * 
	 * If using extension points, no need to set the icon path, the extension point
	 * will read the icon for the generator.
	 * 
	 * @return
	 */
	public String getIconPath();
	public void setIconPath(String path);
}
