package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.PointsValidationException;
import org.eclipse.scanning.api.points.models.SpiralModel;

class SpiralGenerator extends AbstractGenerator<SpiralModel> {

	SpiralGenerator() {
		setLabel("Spiral");
		setDescription("Creates a spiral scaled around the center of a bounding box.");
		setIconPath("icons/scanner--spiral.png"); // This icon exists in the rendering bundle 
	}
	
	@Override
	public Iterator<IPosition> iteratorFromValidModel() {
		return new SpiralIterator(this);
	}

	@Override
	protected void validateModel() {
		super.validateModel();
		if (model.getScale() == 0.0) throw new PointsValidationException("Scale must be non-zero!", model, "scale");
	}
}
