package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.PointsValidationException;
import org.eclipse.scanning.api.points.models.StepModel;

class StepGenerator extends AbstractGenerator<StepModel> {
	
	StepGenerator() {
		setLabel("Step Scan");
		setDescription("Creates a step scan.\nIf the last requested point is within 1%\nof the end it will still be included in the scan");
	}

	@Override
	protected void validateModel() {
		double div = ((model.getStop()-model.getStart())/model.getStep());
		if (div < 0) throw new PointsValidationException("Model step is directed in the wrong direction!", model, "start", "stop", "step");
		if (!Double.isFinite(div)) throw new PointsValidationException("Model step size must be nonzero!", model, "start", "stop", "step");
	}

	@Override
	public int sizeOfValidModel() throws GeneratorException {
		if (containers!=null) throw new GeneratorException("Cannot deal with regions in a step scan!");
		double div = ((model.getStop()-model.getStart())/model.getStep());
		div += (model.getStep() / 100); // add tolerance of 1% of step value
		return (int)Math.floor(div+1);
	}
	
	@Override
	public Iterator<IPosition> iteratorFromValidModel() {
		return new StepIterator(this);
	}

}
