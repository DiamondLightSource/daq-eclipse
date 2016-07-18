package org.eclipse.scanning.points;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.PointsValidationException;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.LissajousModel;

public class LissajousGenerator extends AbstractGenerator<LissajousModel> {

	@Override
	public Iterator<IPosition> iteratorFromValidModel() {
		return new LissajousIterator(this);
	}

	@Override
	protected void validateModel() {
		if (model.getPoints() < 1) throw new PointsValidationException("Must have one or more points in model!");
	}

// Original implementation of createPoints() TODO delete this

	@Override
	public List<IPosition> createPoints() throws GeneratorException {

        validateModel();
        
        ScanPointGenerator spg = new ScanPointGenerator();
	    BoundingBox box = model.getBoundingBox();

        double width = box.getFastAxisLength();
        double height = box.getSlowAxisLength();
        double[] centre = {box.getFastAxisStart() + width / 2, box.getSlowAxisStart() + height / 2};
        
        HashMap<String, Object> box_dict = new HashMap<String, Object>();
        box_dict.put("width", String.valueOf(width));
        box_dict.put("height", String.valueOf(height));
        box_dict.put("centre", Arrays.toString(centre));
        
        String[] names = {"'x'", "'y'"};
        int numLobes = (int) (model.getA() / model.getB());
        int numPoints = model.getPoints();
        boolean alternateDirection = false;
		
		List<IPosition> points = spg.createLissajousPoints(names, "mm", box_dict, numLobes, numPoints, alternateDirection);
        
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

}
