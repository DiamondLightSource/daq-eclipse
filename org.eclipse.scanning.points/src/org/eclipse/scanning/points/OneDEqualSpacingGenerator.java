package org.eclipse.scanning.points;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.OneDEqualSpacingModel;

public class OneDEqualSpacingGenerator extends AbstractGenerator<OneDEqualSpacingModel,Point> {

	// TODO FIXME implement iterator() and size()?
	
	@Override
	public List<Point> createPoints() throws GeneratorException {
		
		if (model.getPoints()<1) throw new GeneratorException("Must have one or more points in model!");
		if (containers==null) throw new GeneratorException("For "+getClass().getName()+" a "+LinearROI.class.getName()+" must be provided!");
		if (containers.size()!=1) throw new GeneratorException("For "+getClass().getName()+" a single "+LinearROI.class.getName()+" must be provided!");
		LinearROI roi = (LinearROI)containers.get(0).getROI();

		double length = model.getBoundingLine().getLength();
		double proportionalStep = (length / model.getPoints()) / length;
		double start = proportionalStep / 2;
		
		List<Point> pointsList = new ArrayList<>();
		for (int i = 0; i < model.getPoints(); i++) {
			// LinearROI has a helpful getPoint(double) method which returns coordinates of a point at a normalised
			// distance along the line
			double[] pointArray = roi.getPoint(start + i * proportionalStep);
			pointsList.add(new Point(i, pointArray[0], i, pointArray[1])); // TODO Are indices correct?
		}
		return pointsList;
	}

}
