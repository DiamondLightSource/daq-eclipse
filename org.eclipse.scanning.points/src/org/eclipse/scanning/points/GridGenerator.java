package org.eclipse.scanning.points;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.GridModel;

class GridGenerator extends AbstractGenerator<GridModel> {

	@Override
	public int size() throws GeneratorException {
		
		if (model.getAngle() == 0.0) {
			// Need to update the xStep and yStep in this call
			// Get the info from the bounding rectangle
            createSteps();
			return model.getColumns() * model.getRows();
		}
		return super.size(); // Slow
	}

	private void createSteps() {
		double xLength = model.getxLength();
		double yLength = model.getyLength();

		// Calculate the required step size
		model.setxStep(xLength / model.getColumns());
		model.setyStep(yLength / model.getRows());
	}

	@Override
	public Iterator<Point> iterator() throws GeneratorException {
		createSteps();
		return new GridIterator(model, container);
	}

	@Override
	public List<Point> createPoints() throws GeneratorException {
		
        createSteps();

		// Calculate the start coordinates
		double minX = model.getMinX() + model.getxStep() / 2;
		double minY = model.getMinY() + model.getyStep() / 2;

		// Create a list of points
		List<Point> pointList = new ArrayList<>(model.getColumns() * model.getRows());

		// Start generating points
		if (model.isBiDirectional()) {
			for (int i = 0; i < model.getRows(); i++) {
				for (int j = 0; j < model.getColumns(); j++) {
					double x = minX + j * model.getxStep();
					double y = minY + i * model.getyStep();
					// Check if point is inside the roi if so add it to the list
					if (container==null) {
						pointList.add(new Point(x, y));
						continue;
					}
					if (container.containsPoint(x, y)) {
						pointList.add(new Point(x, y));
					}
				}
				// Move to the next line and go in the opposite direction
				i++;
				for (int j = model.getColumns(); j >= 0; j--) {
					double x = minX + j * model.getxStep();
					double y = minY + i * model.getyStep();
					// Check if point is inside the roi if so add it to the list
					if (container==null) {
						pointList.add(new Point(x, y));
						continue;
					}
					if (container.containsPoint(x, y)) {
						pointList.add(new Point(x, y));
					}
				}
			}
		}
		// Unidirectional
		else {
			for (int i = 0; i < model.getRows(); i++) {
				for (int j = 0; j < model.getColumns(); j++) {
					double x = minX + j * model.getxStep();
					double y = minY + i * model.getyStep();
					// Check if point is inside the roi if so add it to the list
					if (container==null) {
						pointList.add(new Point(x, y));
						continue;
					}
					if (container.containsPoint(x, y)) {
						pointList.add(new Point(x, y));
					}
				}
			}
		}

		return pointList;
	}

}
