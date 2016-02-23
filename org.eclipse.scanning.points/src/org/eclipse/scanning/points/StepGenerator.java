package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.StepModel;

public class StepGenerator extends AbstractGenerator<StepModel, IPosition> {
	
	StepGenerator() {
		setLabel("Step Scan");
		setDescription("Creates a step scan.\nIf the last requested point is within 1%\nof the end it will still be included in the scan");
	}

	@Override
	protected boolean isValidModel(StepModel model) {
		double div = ((model.getStop()-model.getStart())/model.getStep());
		return !(div < 0);  // This means NaN is considered valid.
		// Therefore .createGenerator(new StepModel()) will still work.
		// But .createGenerator(new StepModel("myStepModel", 0, 10, 0)).createPoints() will hang!
		// TODO: Be stricter about this.
	}

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

}
