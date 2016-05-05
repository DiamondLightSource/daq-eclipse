package org.eclipse.scanning.points;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.IScanPathModel;

class CompoundGenerator extends AbstractGenerator<IScanPathModel> {
	
	private IPointGenerator<?>[] generators;

	public CompoundGenerator(IPointGenerator<?>[] generators) throws GeneratorException {
		super(createId(generators));
        if (generators == null || generators.length<1) throw new GeneratorException("Cannot make a compound generator from a list of less than one generators!");
	    this.generators = generators;
		setLabel("Compound");
		setDescription("Compound generator used when wrapping scans.");
		setVisible(false);
	}
	
	private static String createId(IPointGenerator<?>[] gens) throws GeneratorException {
        if (gens == null || gens.length<1) throw new GeneratorException("Cannot make a compound generator from a list of less than one generators!");
	
        final StringBuilder buf = new StringBuilder();
        for (IPointGenerator<?> gen : gens) buf.append("+"+gen);
        return buf.toString();
	}

	@Override
	protected void validateModel() {
		// CompoundGenerator is a bit of a special case. this.iterator() calls
		// the .iterator() methods of the component IPointGenerators, which in
		// turn each calls .validateModel(). Therefore we don't need to do any
		// explicit validation here.
	}

	@Override
	public int sizeOfValidModel() throws GeneratorException {
		int size = 1;
		for (int i = 0; i < generators.length; i++) size*=generators[i].size();
        return size;
	}
	
	@Override
	protected Iterator<IPosition> iteratorFromValidModel() {
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
		
		IPointGenerator<?> gen = generators[igen];
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

	public IPointGenerator<?>[] getGenerators() {
		return generators;
	}

}
