package org.eclipse.scanning.points;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.OneDStepModel;

class OneDStepGenerator extends AbstractGenerator<OneDStepModel,Point> {

	@Override
	public List<Point> createPoints() throws GeneratorException {
		
		if (model.getStep()==0) throw new GeneratorException("The step cannot be zero!");
		if (containers==null) throw new GeneratorException("For "+getClass().getName()+" a "+LinearROI.class.getName()+" must be provided!");
		if (containers.size()!=1) throw new GeneratorException("For "+getClass().getName()+" a single "+LinearROI.class.getName()+" must be provided!");
		
		LinearROI line = (LinearROI)containers.get(0).getROI();

		double length = line.getLength();
		double proportionalStep = model.getStep() / length;
		int steps = (int) Math.floor(length / model.getStep());
		List<Point> points = new ArrayList<>();
		for (int i = 0; i <= steps; i++) {
			// LinearROI has a helpful getPoint(double) method which returns coordinates of a point at a normalised
			// distance along the line
			double[] pointArray = line.getPoint(i * proportionalStep);
			points.add(new Point(i, pointArray[0], i, pointArray[1])); // TODO Indices might be wrong
		}
		return points;
	}
}
