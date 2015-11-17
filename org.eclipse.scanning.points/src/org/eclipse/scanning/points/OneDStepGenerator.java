package org.eclipse.scanning.points;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.OneDStepModel;

class OneDStepGenerator extends AbstractGenerator<OneDStepModel> {

	@Override
	public List<Point> createPoints() throws GeneratorException {
		
		if (model.getStep()==0) throw new GeneratorException("The step cannot be zero!");
		if (container==null) throw new GeneratorException("For "+getClass().getName()+" a "+LinearROI.class.getName()+" must be provided!");
		
		LinearROI line = (LinearROI)container.getROI();

		double length = line.getLength();
		double proportionalStep = model.getStep() / length;
		int steps = (int) Math.floor(length / model.getStep());
		List<Point> points = new ArrayList<>();
		for (int i = 0; i <= steps; i++) {
			// LinearROI has a helpful getPoint(double) method which returns coordinates of a point at a normalised
			// distance along the line
			double[] pointArray = line.getPoint(i * proportionalStep);
			points.add(new Point(pointArray[0], pointArray[1]));
		}
		return points;
	}
}
