package org.eclipse.scanning.points;

import java.util.Iterator;
import java.util.List;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.PointsValidationException;
import org.eclipse.scanning.api.points.models.BoundingLine;
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

        ScanPointGenerator spg = new ScanPointGenerator();
        BoundingLine line = model.getBoundingLine();
        
		int numPoints = (int) Math.floor(line.getLength() / model.getStep()) + 1;
        double xStep = model.getStep() * Math.cos(line.getAngle());
        double yStep = model.getStep() * Math.sin(line.getAngle());
        
        String[] names = {String.format("'%s'", model.getxName()), String.format("'%s'", model.getyName())};
        double[] start = {line.getxStart(), line.getyStart()};
        double[] stop = {line.getxStart() + xStep * numPoints, line.getyStart() + yStep * numPoints};
        
        List<IPosition> points = spg.create2DLinePoints(names, "mm", start, stop, numPoints);
		return points;
	}
}
