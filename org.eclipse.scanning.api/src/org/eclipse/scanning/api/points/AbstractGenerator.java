package org.eclipse.scanning.api.points;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author fcp94556
 *
 * @param <T>
 * @param <P>
 */
public abstract class AbstractGenerator<T,P> implements IGenerator<T,P>, Iterable<P> {

	protected T model;
	protected List<IPointContainer<?>> containers;

	@Override
	public T getModel() {
		return model;
	}

	@Override
	public void setModel(T model) {
		this.model = model;
	}
	
	/**
	 * Please override this method, the default creates all points and 
	 * returns their size
	 */
	@Override
	public int size() throws GeneratorException {
		// For those generators which implement an iterator,
		// doing this loop is *much* faster for large arrays
		// because memory does not have to be allocated.
		Iterator<P> it = iterator();
		int index = -1;
		while(it.hasNext()) {
			it.next();
			index++;
		}
		return index+1;
	}
	
	@Override
	public List<P> createPoints() throws GeneratorException {
		final List<P> points = new ArrayList<P>(89);
		Iterator<P> it = iterator();
		while(it.hasNext()) points.add(it.next());
		return points;
	}

	@Override
	public List<IPointContainer<?>> getContainers() {
		return containers;
	}

	@Override
	public void setContainers(List<IPointContainer<?>> containers) throws GeneratorException {
		this.containers = containers;
	}
	
	/**
	 * If there are no containers, the point is considered contained.
	 * 
	 * @param x
	 * @param y
	 */
	public boolean containsPoint(double x, double y) {
		if (containers==null)    return true;
		if (containers.size()<1) return true;
		for (IPointContainer<?> container : containers) {
			if (container.containsPoint(x, y)) return true;
		}
		return false;
	}

}
