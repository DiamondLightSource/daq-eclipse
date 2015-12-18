package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.RasterModel;

class RasterGenerator extends AbstractGenerator<RasterModel,Point> {
	

	public Iterator<Point> iterator() {
		return new RasterIterator(this);
	}

// Original implementation of createPoints() TODO delete this
	
//	@Override
//	public List<Point> createPoints() throws GeneratorException {
//		
//		// Get the info from the bounding rectangle
//		double minX = model.getBoundingBox().getxStart();
//		double minY = model.getBoundingBox().getyStart();
//		double xLength = model.getBoundingBox().getWidth();
//		double yLength = model.getBoundingBox().getHeight();
//
//		// Create a list of points
//		int listSizeEstimate = (int) ((Math.floor(xLength / model.getxStep()) + 1) * (Math.floor(yLength / model.getyStep()) + 1));
//		List<Point> pointList = new ArrayList<>(listSizeEstimate);
//
//		// Start generating points
//		int ix=-1, iy=-1;
//		if (model.isSnake()) {
//			for (double y = minY; y <= (minY + yLength); y += model.getyStep()) {
//				// Initialise x outside for so it can be iterated over in both directions
//				double x = minX;
//				++iy;
//				for (; x <= (minX + xLength); x += model.getxStep()) {
//					++ix;
//					// Check if point is inside the roi if so add it to the list
//					if (containsPoint(x, y)) pointList.add(new Point(ix, x, iy, y));
//				}
//				// Move to the next line and go in the opposite direction
//				y += model.getyStep();
//				++iy;
//				for (; x >= minX; x -= model.getxStep()) {
//					++ix;
//					// Check if point is inside the roi if so add it to the list
//					if (containsPoint(x, y)) pointList.add(new Point(ix, x, iy, y));
//				}
//			}
//		}
//		// Uni-directional
//		else {
//			for (double y = minY; y <= (minY + yLength); y += model.getyStep()) {
//				++iy;
//				for (double x = minX; x <= (minX + xLength); x += model.getxStep()) {
//					// Check if point is inside the roi if so add it to the list
//					++ix;
//					if (containsPoint(x, y)) pointList.add(new Point(ix, x, iy, y));
//				}
//			}
//		}
//		return pointList;
//	}

}
