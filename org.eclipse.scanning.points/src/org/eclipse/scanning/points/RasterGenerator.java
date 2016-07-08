package org.eclipse.scanning.points;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointContainer;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.PointsValidationException;
import org.eclipse.scanning.api.points.models.RasterModel;

class RasterGenerator extends AbstractGenerator<RasterModel> {
	
	RasterGenerator() {
		setLabel("Raster");
		setDescription("Creates a raster scan (a scan of x and y).\nThe scan supports bidirectional or 'snake' mode.");
		setIconPath("icons/scanner--raster.png"); // This icon exists in the rendering bundle 
	}

	@Override
	protected void validateModel() {
		if (model.getBoundingBox() == null) throw new PointsValidationException("Model must have a BoundingBox!");
		if (model.getFastAxisStep() == 0) throw new PointsValidationException("Model fast axis step size must be nonzero!");
		if (model.getSlowAxisStep() == 0) throw new PointsValidationException("Model slow axis step size must be nonzero!");

		// Technically the following two throws are not required
		// (The generator could simply produce an empty list.)
		// but we throw errors to avoid potential confusion.
		// Plus, this is consistent with the StepGenerator behaviour.
		if (model.getFastAxisStep()/model.getBoundingBox().getFastAxisLength() < 0)
			throw new PointsValidationException("Model fast axis step is directed so as to produce no points!");
		if (model.getSlowAxisStep()/model.getBoundingBox().getSlowAxisLength() < 0)
			throw new PointsValidationException("Model slow axis step is directed so as to produce no points!");
	}
	
	@Override
    public List<IPosition> createPoints() throws GeneratorException {
        
        validateModel();
        
        ScanPointGenerator spg = new ScanPointGenerator();
        
        double start = model.getBoundingBox().getFastAxisStart();
        int numPoints = (int) Math.floor(model.getBoundingBox().getFastAxisLength() / model.getFastAxisStep() + 1);
        double stop = start + (numPoints - 1) * model.getFastAxisStep();
        
        HashMap<String, Object> inner = new HashMap<String, Object>();
        inner.put("name", model.getFastAxisName());
        inner.put("units", "mm");
        inner.put("start", start);
        inner.put("stop", stop);
        inner.put("num_points", numPoints);

        start = model.getBoundingBox().getSlowAxisStart();
        numPoints = (int) Math.floor(model.getBoundingBox().getSlowAxisLength() / model.getSlowAxisStep() + 1);
        stop = start + (numPoints - 1) * model.getSlowAxisStep();
        
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

	public Iterator<IPosition> iteratorFromValidModel() {
		return new GridIterator(this);
	}

}
