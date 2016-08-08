package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.PointsValidationException;
import org.eclipse.scanning.api.points.models.GridModel;

class GridGenerator extends AbstractGenerator<GridModel> {
	
	GridGenerator() {
		setLabel("Grid");
		setDescription("Creates a grid scan (a scan of x and y).\nThe scan supports bidirectional or 'snake' mode.");
		setIconPath("icons/scanner--grid.png"); // This icon exists in the rendering bundle 
	}

	@Override
	protected void validateModel() {
		// As implemented, model width and/or height can be negative,
		// and this flips the slow and/or fast point order.
		if (model.getSlowAxisPoints() <= 0) throw new PointsValidationException("Model must have a positive number of slow axis points!", model, "slowAxisPoints");
		if (model.getFastAxisPoints() <= 0) throw new PointsValidationException("Model must have a positive number of fast axis points!", model, "fastAxisPoints");
		if (model.getBoundingBox() == null) throw new PointsValidationException("The model must have a Bounding Box!", model, "boundingBox");
	}

	@Override
	public Iterator<IPosition> iteratorFromValidModel() {
		return new GridIterator(this);
	}

}
