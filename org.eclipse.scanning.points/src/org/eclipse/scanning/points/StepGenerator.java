package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.StepModel;

public class StepGenerator extends AbstractGenerator<StepModel, IPosition> {
	
	@Override
	public int size() throws GeneratorException {
		if (containers!=null) throw new GeneratorException("Cannot deal with regions in a step scan!");
		double div = ((model.getStop()-model.getStart())/model.getStep());
		div += (model.getStep() / 100); // add tolerance of 1% of step value
		return (int)Math.floor(div+1);
	}
	
	@Override
	public Iterator<IPosition> iterator() {
		return new StepIterator(this);
	}

// Original implementation of createPoints() TODO delete this

//	@Override
//	public List<IPosition> createPoints() throws GeneratorException {
//		if (containers!=null) throw new GeneratorException("Cannot deal with regions in a step scan!");
//		final List<IPosition> ret = new ArrayList<IPosition>(size());
//		int index = 0;
//		for (double val = model.getStart(); val <= model.getStop(); val+=model.getStep()) {
//			ret.add(new Scalar(model.getName(), index, val));
//			++index;
//		}
//		return ret;
//	}

}
