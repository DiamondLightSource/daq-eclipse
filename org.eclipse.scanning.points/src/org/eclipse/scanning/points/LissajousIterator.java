package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.IPointContainer;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.LissajousModel;

class LissajousIterator implements Iterator<Point> {

	private LissajousModel model;
	private double theta, x, y;
	private IPointContainer container;
	
	// TODO work out if we should only allow closed paths?
	// TODO need to calculate maxTheta needed to close the path.
	private static final double maxTheta = 20 * Math.PI;


	public LissajousIterator(LissajousModel model, IPointContainer container) {
		this.model     = model;
		this.container = container;
	}

	@Override
	public boolean hasNext() {
		double[] da = increment(model, theta);
		double theta = da[0];
		return theta <= theta;
	}

	private final static double[] increment(LissajousModel model, double theta) {
		
		double A = model.getxLength() / 2;
		double B = model.getyLength() / 2;
		double xCentre = model.getX() + A;
		double yCentre = model.getY() + B;
		
		double x = xCentre + A * Math.sin(model.getA() * theta + model.getDelta());
		double y = yCentre + B * Math.cos(model.getB() * theta);
		theta += model.getThetaStep();
		
		return new double[]{theta, x, y};
	}

	@Override
	public Point next() {
		
		double[] da  = increment(model, theta);
		double theta = da[0];
		double x     = da[1];
		double y     = da[2];

		if (theta > maxTheta) return null;
		
		if (container!=null) {
			if (container.containsPoint(x, y)) {
				return new Point(x, y);
			} else {
				return next();
			}
		} else {
			return new Point(x, y);
		}

	}

}
