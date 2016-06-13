package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.PointsValidationException;
import org.eclipse.scanning.api.points.models.SpiralModel;

public class SpiralGenerator extends AbstractGenerator<SpiralModel> {

	@Override
	public Iterator<IPosition> iteratorFromValidModel() {
		return new SpiralIterator(this);
	}

	@Override
	protected void validateModel() {
		if (model.getScale() == 0.0) throw new PointsValidationException("Scale must be non-zero!");
	}
}
