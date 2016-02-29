package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.GridModel;

class GridGenerator extends AbstractGenerator<GridModel,Point> {
	
	GridGenerator() {
		setLabel("Grid");
		setDescription("Creates a grid scan (a scan of x and y).\nThe scan supports bidirectional or 'snake' mode.");
		setIconPath("icons/scanner--grid.png"); // This icon exists in the rendering bundle 
	}

	@Override
	protected void validateModel(GridModel model) throws GeneratorException {
		// As implemented, model width and/or height can be negative,
		// and this flips the "x" and/or "y" point order.
		if (model.getRows() <= 0) throw new GeneratorException("Model must have a positive number of rows!");
		if (model.getColumns() <= 0) throw new GeneratorException("Model must have a positive number of columns!");
	}

	@Override
	public Iterator<Point> iterator() {
		return new GridIterator(this);
	}

}
