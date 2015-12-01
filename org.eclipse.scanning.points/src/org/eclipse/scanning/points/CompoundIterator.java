package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IGenerator;
import org.eclipse.scanning.api.points.IPosition;

/**
 * We are trying to make it super efficient to iterate
 * compound generators by doing this. Otherwise the createPoints(...) 
 * would do.
 * 
 * @author fcp94556
 *
 */
public class CompoundIterator implements Iterator<IPosition> {

	private CompoundGenerator     gen;
	private IPosition             pos;
	private Iterator<? extends IPosition>[] iterators;

	public CompoundIterator(CompoundGenerator gen) throws GeneratorException {
		this.gen       = gen;
		this.iterators = initIterators();
		this.pos       = initPosition();
	}

	@Override
	public boolean hasNext() {
		return iterators[0].hasNext();
	}

	@Override
	public IPosition next() {
		
		for (int i = iterators.length-1; i > -1; i--) {
			if (iterators[i].hasNext()) {
				pos = iterators[i].next().composite(pos);
				return pos;
			} else if (i>0){
				iterators[i] = gen.getGenerators()[i].iterator();
			}
		}
		return null;
	}

	private IPosition initPosition() {
		
		IPosition pos=null;
		for (int i = 0; i < iterators.length-1; i++) {
			pos = iterators[i].next().composite(pos);
		}
		return pos;
	}

	private Iterator<? extends IPosition>[] initIterators() {
		final IGenerator<?,? extends IPosition>[] gs = gen.getGenerators();
		Iterator<? extends IPosition>[] ret = new Iterator[gs.length];
		for (int i = 0; i < gs.length; i++) {
			ret[i] = gs[i].iterator();
		}
		return ret;
	}

}
