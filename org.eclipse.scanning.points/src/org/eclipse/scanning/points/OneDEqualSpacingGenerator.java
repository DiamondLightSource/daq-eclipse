package org.eclipse.scanning.points;

import java.util.Iterator;
import java.util.List;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.PointsValidationException;
import org.eclipse.scanning.api.points.models.BoundingLine;
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
		
		int numPoints = model.getPoints();
		double step = line.getLength() / (numPoints - 1);
		double xStep = step * Math.cos(line.getAngle());
		double yStep = step * Math.sin(line.getAngle());
		
		String[] names = {String.format("'%s'", model.getxName()), String.format("'%s'", model.getyName())};
		double[] start = {line.getxStart() + xStep/2, line.getyStart() + yStep/2};
		double[] stop = {line.getxStart() + xStep * (numPoints - 0.5), line.getyStart() + yStep * (numPoints - 0.5)};
		
		List<IPosition> pointsList = spg.create2DLinePoints(names, "mm", start, stop, numPoints);
		return pointsList;
	}
}
