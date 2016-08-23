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
	private LissajousGenerator gen;
	
	private Point currentPoint;

	public LissajousIterator(LissajousGenerator gen) {
		this.model     = gen.getModel();
		this.gen       = gen;

		String xName = model.getFastAxisName();
		String yName = model.getSlowAxisName();
		double width = model.getBoundingBox().getFastAxisLength();
		double height = model.getBoundingBox().getSlowAxisLength();
		
        JythonObjectFactory lissajousGeneratorFactory = ScanPointGeneratorFactory.JLissajousGeneratorFactory();

        PyDictionary box = new PyDictionary();
        box.put("width", width);
        box.put("height", height);
        box.put("centre", new double[] {model.getBoundingBox().getFastAxisStart() + width / 2,
        								model.getBoundingBox().getSlowAxisStart() + height / 2});

        PyList names =  new PyList(Arrays.asList(new String[] {xName, yName}));
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
