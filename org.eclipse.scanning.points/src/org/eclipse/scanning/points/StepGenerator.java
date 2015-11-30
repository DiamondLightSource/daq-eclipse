package org.eclipse.scanning.points;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.points.models.StepModel;

public class StepGenerator extends AbstractGenerator<StepModel, IPosition> {
	
	@Override
	public int size() {
		double div = ((model.getStop()-model.getStart())/model.getStep());
		return (int)Math.floor(div+1);
	}
	
	

	@Override
	public List<IPosition> createPoints() throws GeneratorException {
		
		final List<IPosition> ret = new ArrayList<IPosition>(size());
		for (double val = model.getStart(); val <= model.getStop(); val+=model.getStep()) {
			ret.add(new Scalar(model.getName(), val));
		}
		return ret;
	}

}
