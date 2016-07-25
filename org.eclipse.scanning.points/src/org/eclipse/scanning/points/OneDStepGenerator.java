package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.PointsValidationException;
import org.eclipse.scanning.api.points.models.OneDStepModel;

class OneDStepGenerator extends AbstractGenerator<OneDStepModel> {

	OneDStepGenerator() {
		setLabel("Point");
		setDescription("Creates a point to scan.");
	}

	@Override
	protected void validateModel() {
		if (model.getStep() <= 0) throw new PointsValidationException("Model step size must be positive!");
	}

	@Override
	protected Iterator<IPosition> iteratorFromValidModel() {
		return new LineIterator(this);
	}
}
