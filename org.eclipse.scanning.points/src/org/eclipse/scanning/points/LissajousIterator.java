package org.eclipse.scanning.points;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.LissajousModel;
import org.eclipse.scanning.points.ScanPointGeneratorFactory.JythonObjectFactory;
import org.python.core.PyDictionary;
import org.python.core.PyList;

class LissajousIterator extends AbstractScanPointIterator {

	private LissajousModel model;
//	private double theta;
	private LissajousGenerator gen;

//	private int pointsSoFar = 0;
	
	private Point currentPoint;

	public LissajousIterator(LissajousGenerator gen) {
		this.model     = gen.getModel();
		this.gen       = gen;
//		this.theta     = -model.getThetaStep();
		
		double width = model.getBoundingBox().getFastAxisLength();
		double height = model.getBoundingBox().getSlowAxisLength();
		
        JythonObjectFactory lissajousGeneratorFactory = ScanPointGeneratorFactory.JLissajousGeneratorFactory();

        PyDictionary box = new PyDictionary();
        box.put("width", width);
        box.put("height", height);
        box.put("centre", new double[] {model.getBoundingBox().getFastAxisStart() + width / 2,
        								model.getBoundingBox().getSlowAxisStart() + height / 2});

        PyList names = new PyList(Arrays.asList(new String[] {model.getFastAxisName(),
        														model.getSlowAxisName()}));
        int numLobes = (int) (model.getA() / model.getB());
        int numPoints = model.getPoints();
        
        @SuppressWarnings("unchecked")
		Iterator<IPosition> iterator = (Iterator<IPosition>) lissajousGeneratorFactory.createObject(
				names, "mm", box, numLobes, numPoints);
        pyIterator = iterator;
	}

	@Override
	public boolean hasNext() {
		Point point;
		double x;
		double y;
		
		while (pyIterator.hasNext()) {
			point = (Point) pyIterator.next();
			x = point.getX();
			y = point.getY();
			
			if (gen.containsPoint(x, y)) {
				currentPoint = point;
				return true;
			}
		}
		
		return false;

//		double[] pos = increment(model, this.theta);
//		double t = pos[0];
//
//		if (pointsSoFar >= model.getPoints()) return false;
//
//		// TODO replace recursion with a simple loop (more like the SpiralIterator)
//		// (The recursion can lead to a StackOverflowError if many successive points are excluded from the scan by an ROI)
//		if (!gen.containsPoint(pos[1], pos[2])) {
//			this.theta = t;
//			return hasNext();
//		}
//		return true;
	}

//	private static double[] increment(LissajousModel model, double theta) {
//
//		theta += model.getThetaStep();
//
//		double A = model.getBoundingBox().getFastAxisLength() / 2;
//		double B = model.getBoundingBox().getSlowAxisLength() / 2;
//		double xCentre = model.getBoundingBox().getFastAxisStart() + A;
//		double yCentre = model.getBoundingBox().getSlowAxisStart() + B;
//
//		double x = xCentre + A * Math.sin(model.getA() * theta + model.getDelta());
//		double y = yCentre + B * Math.cos(model.getB() * theta);
//
//		return new double[]{theta, x, y};
//	}

	@Override
	public Point next() {
		// TODO: This will return null if called without calling hasNext() and when the
		// ROI will exclude all further points. Raise error if called without hasNext()
		// first, or if point is null?
		if (currentPoint == null) {
			hasNext();
		}
		Point point = currentPoint;
		currentPoint = null;
		
		return point;

//		double[] da  = increment(model, theta);
//		double theta = da[0];
//		double x     = da[1];
//		double y     = da[2];
//
//		if (pointsSoFar >= model.getPoints()) return null;
//		this.theta = theta;
//
//		if (gen.containsPoint(x, y)) {
//			pointsSoFar++;
//			// Ideally, we would create a point with two position coordinates (for X and Y motor positions) but only one
//			// index (for the position along the spiral). Because IPosition requires an index for each named axis, we set
//			// the X index to the "real" index, and the Y index to 0. This will lead to a scan file with the positions
//			// written in a block of size 1 x n, rather than a stack of size n, which currently cannot be visualised
//			// properly but is the closest approximation available to the correct structure.
//			return new Point(model.getFastAxisName(), pointsSoFar, x, model.getSlowAxisName(), 0, y);
//		} else {
//			// TODO replace recursion with a simple loop (more like the SpiralIterator)
//			// (The recursion can lead to a StackOverflowError if many successive points are excluded from the scan by an ROI)
//			return next();
//		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove");
	}
}
