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
		if (model.getxStep() == 0) throw new PointsValidationException("Model x step size must be nonzero!");
		if (model.getyStep() == 0) throw new PointsValidationException("Model y step size must be nonzero!");

		// Technically the following two throws are not required
		// (The generator could simply produce an empty list.)
		// but we throw errors to avoid potential confusion.
		// Plus, this is consistent with the StepGenerator behaviour.
		if (model.getxStep()/model.getBoundingBox().getWidth() < 0)
			throw new PointsValidationException("Model x step is directed so as to produce no points!");
		if (model.getyStep()/model.getBoundingBox().getHeight() < 0)
			throw new PointsValidationException("Model y step is directed so as to produce no points!");
	}

	public Iterator<Point> iteratorFromValidModel() {
		return new GridIterator(this);
	}

}
