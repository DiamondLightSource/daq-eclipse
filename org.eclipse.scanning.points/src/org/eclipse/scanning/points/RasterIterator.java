package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.IPointContainer;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.RasterModel;

public class RasterIterator implements Iterator<Point> {

	private RasterModel        model;
	private double             x,y;
	private RasterGenerator    gen;
	
	public RasterIterator(RasterGenerator gen) {
		this.model = gen.getModel();
		this.gen   = gen;
		this.x = model.getxStart()-model.getxStep();
		this.y = model.getyStart();
	}

	private boolean forewards=true;

	@Override
	public boolean hasNext() {
		double[] next = increment(model, x, y, forewards);
		
		double x = next[0];
		double y = next[1];
		double minX = model.getxStart();
		double minY = model.getyStart();
		
		// TODO should check if the next position is in the container!
		
		if (y<model.getyStart() || y > (minY + model.getHeight())) return false;
		if (x<model.getxStart() || x > (minX + model.getWidth())) return false;
		
		if (!gen.containsPoint(x, y)) {
			this.x = next[0];
			this.y = next[1];
			this.forewards = next[2]==1;
			return hasNext();
		}

		return true;
	}


	private static final double[] increment(RasterModel model, double x, double y, boolean forewards) {
		
		if (model.isSnake()) {
			if (forewards) {
				x += model.getxStep();
				if (x > (model.getxStart() + model.getWidth())) {
					y+=model.getyStep();
					forewards = !forewards;
				}
			} else {
				x -= model.getxStep();
				if (x<0) {
					x=0;
					y+=model.getyStep();
					forewards = !forewards;
				}
			}

		} else {
			x += model.getxStep();
			if (x > (model.getxStart() + model.getWidth())) {
				x=model.getxStart();
				y+=model.getyStep();
			}
		}
		return new double[]{x, y, forewards?1:0}; // Bit slow because makes array object to return int values
	}


	@Override
	public Point next() {
		
		double[] next = increment(model, x, y, forewards);
		this.x = next[0];
		this.y = next[1];
		this.forewards = next[2]==1;

		double minX = model.getxStart();
		double minY = model.getyStart();
		if (y<model.getyStart() || y > (minY + model.getHeight())) return null;
		if (x<model.getxStart() || x > (minX + model.getWidth())) throw new NullPointerException("Unexpected index. The x value was "+x);

		if (gen.containsPoint(x, y)) {
			return new Point(x, y);
		} else {
			return next();
		}
	}

}
