package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.points.models.StepModel;

class StepIterator implements Iterator<IPosition> {

	private StepGenerator gen;
	private StepModel     model;
	private double        value;
	
	public StepIterator(StepGenerator gen) {
		this.gen = gen;
		this.model= gen.getModel();
		value = model.getStart()-model.getStep();
	}

	@Override
	public boolean hasNext() {
		double next = increment();
		return next<=model.getStop();
	}

	private double increment() {
		return value+model.getStep();
	}

	@Override
	public IPosition next() {
		value = increment();
		return new Scalar(model.getName(), value);
	}

}
