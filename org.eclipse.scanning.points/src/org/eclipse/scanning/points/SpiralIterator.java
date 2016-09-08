package org.eclipse.scanning.points;

import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.SpiralModel;
import org.eclipse.scanning.points.ScanPointGeneratorFactory.JythonObjectFactory;
import org.python.core.PyList;

class SpiralIterator extends AbstractScanPointIterator {

	// Constant parameters
	private final SpiralGenerator gen;
	private final String xName;
	private final String yName;
	private final double xCentre;
	private final double yCentre;
	private final double maxRadius;
	
	private Point currentPoint;

	public SpiralIterator(SpiralGenerator gen) {
		
		SpiralModel model = gen.getModel();
		this.gen = gen;
		this.xName = model.getFastAxisName();
		this.yName = model.getSlowAxisName();

		double radiusX = model.getBoundingBox().getFastAxisLength() / 2;
		double radiusY = model.getBoundingBox().getSlowAxisLength() / 2;
		xCentre = model.getBoundingBox().getFastAxisStart() + radiusX;
		yCentre = model.getBoundingBox().getSlowAxisStart() + radiusY;
		maxRadius = Math.sqrt(radiusX * radiusX + radiusY * radiusY);
        
        JythonObjectFactory spiralGeneratorFactory = ScanPointGeneratorFactory.JSpiralGeneratorFactory();

        PyList names =  new PyList(Arrays.asList(new String[] {xName, yName}));
        String units = "mm";
        PyList centre = new PyList(Arrays.asList(new Double[] {xCentre, yCentre}));
        double radius = maxRadius;
        double scale = model.getScale();
        boolean alternate = false;
        
        @SuppressWarnings("unchecked")
		Iterator<IPosition> iterator = (Iterator<IPosition>) spiralGeneratorFactory.createObject(
				names, units, centre, radius, scale, alternate);
		pyIterator = iterator;
	}

	@Override
	public boolean hasNext() {
		Point point;
		
		while (pyIterator.hasNext()) {
			point = (Point) pyIterator.next();
			
			if (gen.containsPoint(point)) {
				currentPoint = point;
				return true;
			}
		}
		
		return false;		
	}

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
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove");
	}
}
