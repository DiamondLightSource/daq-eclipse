package org.eclipse.scanning.points;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.RasterModel;

class RasterGenerator extends AbstractGenerator<RasterModel,Point> {
	

	public Iterator<Point> iterator() {
		return new RasterIterator(this);
	}

	@Override
	public List<Point> createPoints() throws GeneratorException {
		
		// Get the info from the bounding rectangle
		double minX = model.getBoundingBox().getxStart();
		double minY = model.getBoundingBox().getyStart();
		double xLength = model.getBoundingBox().getWidth();
		double yLength = model.getBoundingBox().getHeight();

		// Create a list of points
		int listSizeEstimate = (int) ((Math.floor(xLength / model.getxStep()) + 1) * (Math.floor(yLength / model.getyStep()) + 1));
		List<Point> pointList = new ArrayList<>(listSizeEstimate);

		// Start generating points
		if (model.isSnake()) {
			for (double y = minY; y <= (minY + yLength); y += model.getyStep()) {
				// Initialise x outside for so it can be iterated over in both directions
				double x = minX;
				for (; x <= (minX + xLength); x += model.getxStep()) {
					// Check if point is inside the roi if so add it to the list
					if (containsPoint(x, y)) pointList.add(new Point(x, y));
				}
				// Move to the next line and go in the opposite direction
				y += model.getyStep();
				for (; x >= minX; x -= model.getxStep()) {
					// Check if point is inside the roi if so add it to the list
					if (containsPoint(x, y)) pointList.add(new Point(x, y));
				}
			}
		}
		// Uni-directional
		else {
			for (double y = minY; y <= (minY + yLength); y += model.getyStep()) {
				for (double x = minX; x <= (minX + xLength); x += model.getxStep()) {
					// Check if point is inside the roi if so add it to the list
					if (containsPoint(x, y)) pointList.add(new Point(x, y));
				}
			}
		}
		return pointList;
	}

}
