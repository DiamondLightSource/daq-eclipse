package org.eclipse.scanning.api.points;

import java.util.Iterator;

public abstract class AbstractGenerator<T,P> implements IGenerator<T,P>, Iterable<P> {

	protected T model;
	protected IPointContainer<?> container;

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
			Object next = it.next();
			if (next==null) break;
			index++;
		}
		return index+1;
	}
	
	/**
	 * Please override this method, the default creates all points and 
	 * returns their iterator
	 */
	@Override
	public Iterator<P> iterator() {
		try {
			return createPoints().iterator();
		} catch (GeneratorException e) {
			throw new IllegalArgumentException("Cannot generate an iterator!", e);
		}
	}

	@Override
	public <R> IPointContainer<R> getContainer() {
		return (IPointContainer<R>)container;
	}

	@Override
	public <R> void setContainer(IPointContainer<R> container) {
		this.container = container;
	}

}
