package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.RasterModel;

public class RasterIterator implements Iterator<Point> {

	private RasterModel        model;
	private double             x,y;
	private RasterGenerator    gen;
	private int                ix, iy;
	
	public RasterIterator(RasterGenerator gen) {
		this.model = gen.getModel();
		this.gen   = gen;
		this.x = model.getBoundingBox().getxStart() - model.getxStep();
		this.y = model.getBoundingBox().getyStart();
		ix=iy=-1;
	}

	private boolean forewards=true;

	@Override
	public boolean hasNext() {
		double[] next = increment(model, ix, x, iy, y, forewards);
		
		double x  = next[1];
		double y  = next[3];
		double minX = model.getBoundingBox().getxStart();
		double minY = model.getBoundingBox().getyStart();
		
		// TODO should check if the next position is in the container!
		
		if (y < model.getBoundingBox().getyStart() || y > (minY + model.getBoundingBox().getHeight())) return false;
		if (x < model.getBoundingBox().getxStart() || x > (minX + model.getBoundingBox().getWidth())) return false;
		
		if (!gen.containsPoint(x, y)) {
			this.x = next[1];
			this.y = next[3];
			this.forewards = next[4]==1;
			return hasNext();
		}

		return true;
	}


	private static final double[] increment(RasterModel model, int ix, double x, int iy, double y, boolean forewards) {
		
		if (model.isSnake()) {
			if (forewards) {
				x += model.getxStep();
				++ix;
				if (x > (model.getBoundingBox().getxStart() + model.getBoundingBox().getWidth())) {
					y+=model.getyStep();
					++iy;
					forewards = !forewards;
				}
			} else {
				x -= model.getxStep();
				++ix;
				if (x<0) {
					x=0;
					y+=model.getyStep();
					++iy;
					forewards = !forewards;
				}
			}

		} else {
			x += model.getxStep();
			++ix;
			if (x > (model.getBoundingBox().getxStart() + model.getBoundingBox().getWidth())) {
				x = model.getBoundingBox().getxStart();
				y += model.getyStep();
				++iy;
			}
		}
		return new double[]{ix, x, iy, y, forewards?1:0}; // Bit slow because makes array object to return int values
	}


	@Override
	public Point next() {
		
		double[] next = increment(model, ix, x, iy, y, forewards);
		this.ix = (int)next[0];
		this.x  = next[1];
		this.iy = (int)next[2];
		this.y  = next[3];
		this.forewards = next[4]==1;

		double minX = model.getBoundingBox().getxStart();
		double minY = model.getBoundingBox().getyStart();
		if (y < model.getBoundingBox().getyStart() || y > (minY + model.getBoundingBox().getHeight())) return null;
		if (x < model.getBoundingBox().getxStart() || x > (minX + model.getBoundingBox().getWidth())) throw new NullPointerException("Unexpected index. The x value was "+x);

		if (gen.containsPoint(x, y)) {
			return new Point(ix, x, iy, y);
		} else {
			return next();
		}
	}

	public void remove() {
        throw new UnsupportedOperationException("remove");
    }

}
