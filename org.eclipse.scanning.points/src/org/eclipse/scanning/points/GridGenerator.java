package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.GridModel;

class GridGenerator extends AbstractGenerator<GridModel,Point> {

	@Override
	public Iterator<Point> iterator() {
		return new GridIterator(this);
	}

// Original implementation of createPoints() TODO delete this

//	@Override
//	public List<Point> createPoints() throws GeneratorException {
//
//		createSteps();
//
//		// Calculate the start coordinates
//		double minX = model.getBoundingBox().getxStart() + model.getxStep() / 2;
//		double minY = model.getBoundingBox().getyStart() + model.getyStep() / 2;
//
//		// Create a list of points
//		List<Point> pointList = new ArrayList<>(model.getColumns() * model.getRows());
//
//		// Start generating points
//		if (model.isSnake()) {
//			for (int i = 0; i < model.getRows(); i++) {
//				for (int j = 0; j < model.getColumns(); j++) {
//					double x = minX + j * model.getxStep();
//					double y = minY + i * model.getyStep();
//					// Check if point is inside the roi if so add it to the list
//					if (containsPoint(x, y)) {
//						pointList.add(new Point(i, x, j, y));
//					}
//				}
//				// Move to the next line and go in the opposite direction
//				i++;
//				for (int j = model.getColumns(); j >= 0; j--) {
//					double x = minX + j * model.getxStep();
//					double y = minY + i * model.getyStep();
//					// Check if point is inside the roi if so add it to the list
//					if (containsPoint(x, y)) {
//						pointList.add(new Point(i, x, j, y));
//					}
//				}
//			}
//		}
//		// Unidirectional
//		else {
//			for (int i = 0; i < model.getRows(); i++) {
//				for (int j = 0; j < model.getColumns(); j++) {
//					double x = minX + j * model.getxStep();
//					double y = minY + i * model.getyStep();
//					// Check if point is inside the roi if so add it to the list
//					if (containsPoint(x, y)) {
//						pointList.add(new Point(i, x, j, y));
//					}
//				}
//			}
//		}
//
//		return pointList;
//	}

}
