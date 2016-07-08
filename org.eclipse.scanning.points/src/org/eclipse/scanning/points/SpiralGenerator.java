package org.eclipse.scanning.points;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.PointsValidationException;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.SpiralModel;

public class SpiralGenerator extends AbstractGenerator<SpiralModel> {

	@Override
	public Iterator<IPosition> iteratorFromValidModel() {
		return new SpiralIterator(this);
	}
	
    @Override
    public List<IPosition> createPoints() throws GeneratorException {
        
        validateModel();
        
        ScanPointGenerator spg = new ScanPointGenerator();
        BoundingBox box = model.getBoundingBox();
        
        double radiusX = model.getBoundingBox().getFastAxisLength() / 2;
        double radiusY = model.getBoundingBox().getSlowAxisLength() / 2;
        
        String[] names = {String.format("'%s'", model.getFastAxisName()), String.format("'%s'", model.getSlowAxisName())};
        String units = "mm";
        double[] centre = {box.getFastAxisStart() + radiusX, box.getSlowAxisStart() + radiusY};
        double radius = Math.sqrt(radiusX * radiusX + radiusY * radiusY);
        boolean alternateDirection = false;
        
        List<IPosition> points = spg.createSpiralPoints(names, units, centre, radius, model.getScale(), alternateDirection);
        
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
	protected void validateModel() {
		if (model.getScale() == 0.0) throw new PointsValidationException("Scale must be non-zero!");
	}
}
