package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.LissajousModel;

class LissajousIterator implements Iterator<Point> {

	private LissajousModel model;
	private double theta;
	private LissajousGenerator gen;
	
	// TODO work out if we should only allow closed paths?
	// TODO need to calculate maxTheta needed to close the path.
	private static final double maxTheta = 20 * Math.PI;


	public LissajousIterator(LissajousGenerator gen) {
		this.model     = gen.getModel();
		this.gen       = gen;
		this.theta     = -model.getThetaStep();
	}

	@Override
	public boolean hasNext() {
		
		double[] pos = increment(model, this.theta);
		double t = pos[0];
		
		if (t > maxTheta) return false;

		if (!gen.containsPoint(pos[1], pos[2])) {
			this.theta = t;
            return hasNext();
		}
		return true;
	}

	private final double[] increment(LissajousModel model, double theta) {
		
		theta += model.getThetaStep();

		double A = model.getWidth() / 2;
		double B = model.getHeight() / 2;
		double xCentre = model.getxStart() + A;
		double yCentre = model.getyStart() + B;
		
		double x = xCentre + A * Math.sin(model.getA() * theta + model.getDelta());
		double y = yCentre + B * Math.cos(model.getB() * theta);
		
		return new double[]{theta, x, y};
	}

	@Override
	public Point next() {
		
		double[] da  = increment(model, theta);
		double theta = da[0];
		double x     = da[1];
		double y     = da[2];

		if (theta > maxTheta) return null;
		this.theta = theta;

		if (gen.containsPoint(x, y)) {
			return new Point(x, y);
		} else {
			return next();
		}
	}

}
