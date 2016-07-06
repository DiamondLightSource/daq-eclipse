package org.eclipse.scanning.points;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.PointsValidationException;
import org.eclipse.scanning.api.points.models.OneDStepModel;

class OneDStepGenerator extends AbstractGenerator<OneDStepModel> {

	OneDStepGenerator() {
		setLabel("Point");
		setDescription("Creates a point to scan.");
	}

	@Override
	protected void validateModel() {
		if (model.getStep() <= 0) throw new PointsValidationException("Model step size must be positive!", model, "step");
	}

	@Override
	protected Iterator<IPosition> iteratorFromValidModel() {
		try {
			return createPoints().iterator();
		} catch (GeneratorException e) {
			throw new IllegalArgumentException("Cannot generate an iterator!", e);
		}
	}

	@Override
	public List<IPosition> createPoints() throws GeneratorException {
		// FIXME: Make this work with just a bounding line (i.e. no ROI).

		// FIXME: This code can be called without validateModel() ever having been run.
		// Therefore, we call validateModel() manually here. What we should do is move
		// the geometry/maths to iteratorFromValidModel() (in line with the other
		// generators here) rather than overriding AbstractGenerator.createPoints().
		// Then we wouldn't need to manually call validateModel().
		//
		// However, all these generators are to be rewritten in Python (supposedly)
		// so this fix might as well wait until then.
		//
		// For the moment, just call validateModel() here...
		validateModel();
		
		if (containers==null) throw new GeneratorException("For "+getClass().getName()+" a "+LinearROI.class.getName()+" must be provided!");
		if (containers.size()!=1) throw new GeneratorException("For "+getClass().getName()+" a single "+LinearROI.class.getName()+" must be provided!");
		
		LinearROI line = (LinearROI)containers.get(0).getROI();

		double length = line.getLength();
		double proportionalStep = model.getStep() / length;
		int steps = (int) Math.floor(length / model.getStep());
//		List<IPosition> points = new ArrayList<>();
//		for (int i = 0; i <= steps; i++) {
//			// LinearROI has a helpful getPoint(double) method which returns coordinates of a point at a normalised
//			// distance along the line
//			double[] pointArray = line.getPoint(i * proportionalStep);
//			points.add(new Point(i, pointArray[0], i, pointArray[1], false)); // TODO Indices might be wrong
//		}

        ScanPointGenerator spg = new ScanPointGenerator();
        String[] names = {String.format("'%s'", model.getxName()), String.format("'%s'", model.getyName())};
        double[] start = line.getPoint();
        int numPoints = steps + 1;
        double[] stop = line.getEndPoint();
        
        List<IPosition> points = spg.create2DLinePoints(names, "'mm'", start, stop, numPoints, false);
		return points;
	}
}
