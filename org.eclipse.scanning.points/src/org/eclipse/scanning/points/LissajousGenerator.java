package org.eclipse.scanning.points;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.LissajousModel;

class LissajousGenerator extends AbstractGenerator<LissajousModel> {

	
	@Override
	public List<Point> createPoints() throws GeneratorException {
		
		// Pick A and B so we will fill the bounding rectangle
		double A = model.getxLength() / 2;
		double B = model.getyLength() / 2;
		double xCentre = model.getMinX() + A;
		double yCentre = model.getMinY() + B;
		// double maxRadius = Math.sqrt(radiusX * radiusX + radiusY * radiusY);

		List<Point> pointList = new ArrayList<>();

		// TODO work out if we should only allow closed paths?
		// TODO need to calculate maxTheta needed to close the path.
		double maxTheta = 20 * Math.PI;
		double theta = 0;
		while (theta <= maxTheta) {
			double x = xCentre + A * Math.sin(model.getA() * theta + model.getDelta());
			double y = yCentre + B * Math.cos(model.getB() * theta);
			if (container!=null) {
				if (container.containsPoint(x, y)) {
					pointList.add(new Point(x, y));
			     }
			} else {
				pointList.add(new Point(x, y));
			}
			theta += model.getThetaStep();
		}
		return pointList;
	}

}
