package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.PointsValidationException;
import org.eclipse.scanning.api.points.models.RasterModel;

class RasterGenerator extends AbstractGenerator<RasterModel,Point> {
	
	RasterGenerator() {
		setLabel("Raster");
		setDescription("Creates a raster scan (a scan of x and y).\nThe scan supports bidirectional or 'snake' mode.");
		setIconPath("icons/scanner--raster.png"); // This icon exists in the rendering bundle 
	}

	@Override
	protected void validateModel() {
		if (model.getBoundingBox() == null) throw new PointsValidationException("Model must have a BoundingBox!");
		if (model.getFastAxisStep() == 0) throw new PointsValidationException("Model fast axis step size must be nonzero!");
		if (model.getSlowAxisStep() == 0) throw new PointsValidationException("Model slow axis step size must be nonzero!");

		// Technically the following two throws are not required
		// (The generator could simply produce an empty list.)
		// but we throw errors to avoid potential confusion.
		// Plus, this is consistent with the StepGenerator behaviour.
		if (model.getFastAxisStep()/model.getBoundingBox().getFastAxisLength() < 0)
			throw new PointsValidationException("Model fast axis step is directed so as to produce no points!");
		if (model.getSlowAxisStep()/model.getBoundingBox().getSlowAxisLength() < 0)
			throw new PointsValidationException("Model slow axis step is directed so as to produce no points!");
	}

	public Iterator<Point> iteratorFromValidModel() {
		return new GridIterator(this);
	}

}
