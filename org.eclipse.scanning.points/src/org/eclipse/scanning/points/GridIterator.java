package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.GridModel;

class GridIterator implements Iterator<Point> {

	private GridModel model;
	private double    minX;
	private double    minY;
	
	private int i,j;
	private GridGenerator gen;

	public GridIterator(GridGenerator gen) {
		this.model = gen.getModel();	
		this.gen   = gen;
        this.minX = model.getBoundingBox().getxStart() + model.getxStep() / 2;
		this.minY = model.getBoundingBox().getyStart() + model.getyStep() / 2;
        i=0;
        j=-1;
	}
	
	private boolean forewards=true;

	@Override
	public boolean hasNext() {
		
		int[] next = increment(model, i, j, forewards); 
		int i = next[0];
		int j = next[1];
			
		if (i>(model.getRows()-1) || i<0)    {
			return false;  // Normal termination
		}
		if (j>(model.getColumns()-1) || j<0) return false;
		
		double x = minX + j * model.getxStep();
		double y = minY + i * model.getyStep();
		if (!gen.containsPoint(x, y)) {
			this.i = i;
			this.j = j;
			this.forewards = next[2]==1;
			return hasNext();
		}

		return true;
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
		if (gen.containsPoint(x, y)) {
			return new Point(model.getxName(), j, x, model.getyName(), i, y);
		} else {
			return next();
		}
	}

	public void remove() {
        throw new UnsupportedOperationException("remove");
    }

}
