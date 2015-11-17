package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.IPointContainer;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.GridModel;

class GridIterator implements Iterator<Point> {

	private GridModel model;
	private IPointContainer container;
	private double    minX;
	private double    minY;
	
	private int i,j;

	public GridIterator(GridModel model, IPointContainer container) {
		this.model     = model;	
		this.container = container;
        this.minX = model.getX() + model.getxStep() / 2;
		this.minY = model.getY() + model.getyStep() / 2;
        i=0;
        j=-1;
	}
	
	private boolean forewards=true;

	@Override
	public boolean hasNext() {
		int[] next = increment(model, i, j, forewards); 
		return next[0] < model.getRows() && next[1] < model.getColumns();
	}


	private static final int[] increment(GridModel model, int i, int j, boolean forewards) {
		
		if (model.isSnake()) {
			if (forewards) {
				j = j+1;
				if (j>(model.getColumns()-1)) {
					i++;
					forewards = !forewards;
				}
			} else {
				j = j-1;
				if (j<0) {
					j=0;
					i++;
					forewards = !forewards;
				}
			}

		} else {
			j++;
			if (j>(model.getColumns()-1)) {
				j=0;
				i++;
			}
		}
		return new int[]{i,j, forewards?1:0}; // Bit slow because makes array object to return int values
	}

	
	@Override
	public Point next() {
		
		int[] next = increment(model, i, j, forewards);
		this.i = next[0];
		this.j = next[1];
		this.forewards = next[2]==1;
		
		if (i>(model.getRows()-1) || i<0)    return null;  // Normal termination
		if (j>(model.getColumns()-1) || j<0) throw new NullPointerException("Unexpected index. The j index was "+j);

		double x = minX + j * model.getxStep();
		double y = minY + i * model.getyStep();
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
