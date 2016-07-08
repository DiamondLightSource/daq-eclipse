package org.eclipse.scanning.points;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.PointsValidationException;
import org.eclipse.scanning.api.points.models.GridModel;

class GridGenerator extends AbstractGenerator<GridModel> {
	
	GridGenerator() {
		setLabel("Grid");
		setDescription("Creates a grid scan (a scan of x and y).\nThe scan supports bidirectional or 'snake' mode.");
		setIconPath("icons/scanner--grid.png"); // This icon exists in the rendering bundle 
	}

	@Override
	protected void validateModel() {
		// As implemented, model width and/or height can be negative,
		// and this flips the slow and/or fast point order.
		if (model.getSlowAxisPoints() <= 0) throw new PointsValidationException("Model must have a positive number of slow axis points!");
		if (model.getFastAxisPoints() <= 0) throw new PointsValidationException("Model must have a positive number of fast axis points!");
		if (model.getBoundingBox() == null) throw new PointsValidationException("Model must have a BoundingBox!");
	}
    
    @Override
    public List<IPosition> createPoints() throws GeneratorException {
        
        validateModel();
        
        ScanPointGenerator spg = new ScanPointGenerator();
        
        double step = model.getBoundingBox().getFastAxisLength() / model.getFastAxisPoints();
        double start = model.getBoundingBox().getFastAxisStart() + step/2;
        double stop = start + model.getBoundingBox().getFastAxisLength() - step;
        int numPoints = model.getFastAxisPoints();
        
        HashMap<String, Object> inner = new HashMap<String, Object>();
        inner.put("name", model.getFastAxisName());
        inner.put("units", "mm");
        inner.put("start", start);
        inner.put("stop", stop);
        inner.put("num_points", numPoints);

        step = model.getBoundingBox().getSlowAxisLength() / model.getSlowAxisPoints();
        start = model.getBoundingBox().getSlowAxisStart() + step/2;
        stop = start + model.getBoundingBox().getSlowAxisLength() - step;
        numPoints = model.getSlowAxisPoints();

        HashMap<String, Object> outer = new HashMap<String, Object>();
        outer.put("name", model.getSlowAxisName());
        outer.put("units", "mm");
        outer.put("start", start);
        outer.put("stop", stop);
        outer.put("num_points", numPoints);
        
        List<IPosition> points = spg.createRasterPoints(inner, outer, model.isSnake());
        
        List<IPosition> filteredPoints = new ArrayList<IPosition>();
        for (IPosition point: points) {
            double xCoord = (Double) point.get("X");
            double yCoord = (Double) point.get("Y");
            if (containsPoint(xCoord, yCoord)) {
                filteredPoints.add(point);
            }
        }
        return filteredPoints;
    }

	@Override
	public Iterator<IPosition> iteratorFromValidModel() {
		return new GridIterator(this);
	}

}
