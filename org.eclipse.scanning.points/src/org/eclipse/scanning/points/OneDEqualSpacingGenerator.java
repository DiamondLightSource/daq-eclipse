package org.eclipse.scanning.points;

import java.util.Iterator;
import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.PointsValidationException;
import org.eclipse.scanning.api.points.models.OneDEqualSpacingModel;
import org.eclipse.scanning.points.ScanPointGenerator;

public class OneDEqualSpacingGenerator extends AbstractGenerator<OneDEqualSpacingModel> {

	OneDEqualSpacingGenerator() {
		setLabel("Line Equal Spacing");
		setDescription("Creates a line scan along a line defined in two dimensions.");
		setIconPath("icons/scanner--line.png"); // This icon exists in the rendering bundle 
	}

	@Override
	protected void validateModel() {
		if (model.getPoints() < 1) throw new PointsValidationException("Must have one or more points in model!");
	}

	@Override
	public Iterator<IPosition> iteratorFromValidModel() {
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
		LinearROI roi = (LinearROI)containers.get(0).getROI();

//		double length = model.getBoundingLine().getLength();
//		double proportionalStep = (length / model.getPoints()) / length;
//		double start = proportionalStep / 2;
		
//		List<IPosition> pointsList = new ArrayList<>();
//		for (int i = 0; i < model.getPoints(); i++) {
//			// LinearROI has a helpful getPoint(double) method which returns coordinates of a point at a normalised
//			// distance along the line
//			double[] pointArray = roi.getPoint(start + i * proportionalStep);
//			pointsList.add(new Point(model.getxName(), i, pointArray[0], model.getyName(), i, pointArray[1], false));
//		}
//		double step = length / model.getPoints();
		
		ScanPointGenerator spg = new ScanPointGenerator();
		String[] names = {String.format("'%s'", model.getxName()), String.format("'%s'", model.getyName())};
		double[] start = roi.getPoint();
//		TODO: start[0] += step/2 etc
		double[] stop = roi.getEndPoint();
		
		List<IPosition> pointsList = spg.create2DLinePoints(names, "'mm'", start, stop, model.getPoints(), false);
		return pointsList;
	}

}
