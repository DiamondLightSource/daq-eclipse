package org.eclipse.scanning.points;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;

class CompoundGenerator extends AbstractGenerator<Object, IPosition> {
	
	private IPointGenerator<?, ? extends IPosition>[] generators;

	public CompoundGenerator(IPointGenerator<?,? extends IPosition>[] generators) throws GeneratorException {
        if (generators == null || generators.length<1) throw new GeneratorException("Cannot make a compound generator from a list of less than one generators!");
	    this.generators = generators;
	}
	
	@Override
	public int size() throws GeneratorException {
		int size = 1;
		for (int i = 0; i < generators.length; i++) size*=generators[i].size();
        return size;
	}
	
	@Override
	public Iterator<IPosition> iterator() {
		try {
			return new CompoundIterator(this);
		} catch (GeneratorException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public List<IPosition> createPoints() throws GeneratorException {
		
		List<IPosition> points = new ArrayList<>(size());
		createPoints(0, points, null);
		return points;
	}

	/**
	 * This simple recursive method is what nested scans reduce to.
	 * 
	 * @param igen
	 * @param points
	 * @param parent
	 */
	private void createPoints(int igen, List<IPosition> points, IPosition parent) {
		
		IPointGenerator<?,? extends IPosition> gen = generators[igen];
		Iterator<? extends IPosition>     it  = gen.iterator();
		while(it.hasNext()) {
			IPosition pos = it.next().composite(parent);
			int nextGen = igen+1;
			if (nextGen<generators.length) {
				createPoints(nextGen, points, pos);
			} else {
				points.add(pos);
			}
		}
	
	}

	public IPointGenerator<?, ? extends IPosition>[] getGenerators() {
		return generators;
	}

}
