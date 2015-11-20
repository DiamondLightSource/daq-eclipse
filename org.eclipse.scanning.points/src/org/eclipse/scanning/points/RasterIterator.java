package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.IPointContainer;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.RasterModel;

public class RasterIterator implements Iterator<Point> {

	private RasterModel        model;
	private IPointContainer<?> container;
	double  x,y;
	
	public RasterIterator(RasterModel model, IPointContainer<?> container) {
		this.model     = model;
		this.container = container;
		this.x = model.getX()-model.getxStep();
		this.y = model.getY();
	}

	private boolean forewards=true;

	@Override
	public boolean hasNext() {
		double[] next = increment(model, x, y, forewards);
		
		double x = next[0];
		double y = next[1];
		double minX = model.getX();
		double minY = model.getY();
		if (y<model.getY() || y > (minY + model.getyLength())) return false;
		if (x<model.getX() || x > (minX + model.getxLength())) return false;
		
		return true;
	}


	private static final double[] increment(RasterModel model, double x, double y, boolean forewards) {
		
		if (model.isSnake()) {
			if (forewards) {
				x += model.getxStep();
				if (x > (model.getX() + model.getxLength())) {
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
			if (x > (model.getX() + model.getxLength())) {
				x=model.getX();
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

		double minX = model.getX();
		double minY = model.getY();
		if (y<model.getY() || y > (minY + model.getyLength())) return null;
		if (x<model.getX() || x > (minX + model.getxLength())) throw new NullPointerException("Unexpected index. The x value was "+x);

		if (container==null) {
			return new Point(x, y);
		}
		if (container.containsPoint(x, y)) {
			return new Point(x, y);
		} else {
			return next();
		}
	}

}
