package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.AbstractPosition;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.points.ScanPointGeneratorFactory.JythonObjectFactory;

/**
 * We are trying to make it super efficient to iterate
 * compound generators by doing this. Otherwise the createPoints(...) 
 * would do.
 * 
 * @author Matthew Gerring
 *
 */
public class CompoundIterator extends AbstractScanPointIterator {

	private CompoundGenerator     gen;
	private IPosition             pos;
	private Iterator<? extends IPosition>[] iterators;
	
	public Iterator<IPosition> pyIterator;
	private IPosition currentPoint;

	public CompoundIterator(CompoundGenerator gen) throws GeneratorException {
		this.gen       = gen;
		this.iterators = initIterators();
		this.pos       = createFirstPosition();

		JythonObjectFactory compoundGeneratorFactory = ScanPointGeneratorFactory.JCompoundGeneratorFactory();
		
        Object[] excluders = {};
        Object[] mutators = {};
        
		@SuppressWarnings("unchecked")
		Iterator<IPosition> iterator = (Iterator<IPosition>)  compoundGeneratorFactory.createObject(
				iterators, excluders, mutators);
		pyIterator = iterator;
	}

	private IPosition createFirstPosition() throws GeneratorException {
		
	    IPosition pos = new MapPosition();
		for (int i = 0; i < iterators.length-1; i++) {
			pos = iterators[i].next().compound(pos);
		}
		return pos;
	}
	
	@Override
	public boolean hasNext() {
		// TODO: Uncomment this once MapPosition updated and CompoundGenerator more generic
		IPosition point;
//		double x;
//		double y;
		
		while (pyIterator.hasNext()) {
			point = pyIterator.next();
//			x = point.getX();
//			y = point.getY();
//			
//			if (gen.containsPoint(x, y)) {
			currentPoint = point;
			return true;
//			}
		}
		
		return false;
	}

	@Override
	public IPosition next() {
		// TODO: This will return null if called without calling hasNext() and when the
		// ROI will exclude all further points. Raise error if called without hasNext()
		// first, or if point is null?
		if (currentPoint == null) {
			hasNext();
		}
		IPosition point = currentPoint;
		currentPoint = null;
		
		return point;
	}
	
	public IPosition getNext() {
		
		for (int i = iterators.length-1; i > -1; i--) {
			if (iterators[i].hasNext()) {
				IPosition next = iterators[i].next();
				pos = next.compound(pos);
				((AbstractPosition)pos).setDimensionNames(gen.getDimensionNames());
				return pos;
			} else if (i>0) {
				iterators[i]    = gen.getGenerators()[i].iterator();
				IPosition first = iterators[i].next();
				pos = first.compound(pos);
				((AbstractPosition)pos).setDimensionNames(gen.getDimensionNames());
			}
		}
		return null;
	}


	private Iterator<? extends IPosition>[] initIterators() {
		final IPointGenerator<?>[] gs = gen.getGenerators();
		@SuppressWarnings("unchecked")
		Iterator<? extends IPosition>[] ret = new Iterator[gs.length];
		for (int i = 0; i < gs.length; i++) {
			ret[i] = gs[i].iterator();
		}
		return ret;
	}

	public void remove() {
        throw new UnsupportedOperationException("remove");
    }

}
