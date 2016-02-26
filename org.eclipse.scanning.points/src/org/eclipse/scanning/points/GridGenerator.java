package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.GridModel;

class GridGenerator extends AbstractGenerator<GridModel,Point> {
	
	GridGenerator() {
		setLabel("Grid");
		setDescription("Creates a grid scan (a scan of x and y).\nThe scan supports bidirectional or 'snake' mode.");
		setIconPath("icons/scanner--grid.png"); // This icon exists in the rendering bundle 
	}

	@Override
	protected boolean isValidModel(GridModel model) {
		return 0 < model.getRows() && 0 < model.getColumns();
	}

	@Override
	public Iterator<Point> iterator() {
		return new GridIterator(this);
	}

}
