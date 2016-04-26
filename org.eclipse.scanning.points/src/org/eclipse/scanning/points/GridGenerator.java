package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.PointsValidationException;
import org.eclipse.scanning.api.points.models.GridModel;

class GridGenerator extends AbstractGenerator<GridModel,Point> {
	
	GridGenerator() {
		setLabel("Grid");
		setDescription("Creates a grid scan (a scan of x and y).\nThe scan supports bidirectional or 'snake' mode.");
		setIconPath("icons/scanner--grid.png"); // This icon exists in the rendering bundle 
	}

	@Override
	protected void validateModel() {
		// As implemented, model width and/or height can be negative,
		// and this flips the slow and/or fast point order.
		if (model.getSlowAxisPoints() <= 0) throw new PointsValidationException("Model must have a positive number of slow axis points!");
		if (model.getFastAxisPoints() <= 0) throw new PointsValidationException("Model must have a positive number of fast axis points!");
		if (model.getBoundingBox() == null) throw new PointsValidationException("Model must have a BoundingBox!");
	}

	@Override
	public Iterator<Point> iteratorFromValidModel() {
		return new GridIterator(this);
	}

}
