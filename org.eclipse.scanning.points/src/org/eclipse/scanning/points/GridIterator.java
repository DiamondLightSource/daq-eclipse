package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.GridModel;

class GridIterator implements Iterator<Point> {

	private final GridModel model;
	private final GridGenerator gen;
	private final double minX;
	private final double minY;
	private final double xStep;
	private final double yStep;

	private int i,j;
	private boolean forwards=true;

	public GridIterator(GridGenerator gen) {
		this.model = gen.getModel();	
		this.gen   = gen;
		this.xStep = model.getBoundingBox().getWidth() / model.getColumns();
		this.yStep = model.getBoundingBox().getHeight() / model.getRows();
		this.minX = model.getBoundingBox().getxStart() + xStep / 2;
		this.minY = model.getBoundingBox().getyStart() + yStep / 2;
		i = 0;
		j = -1;
	}

	@Override
	public boolean hasNext() {
		
		int[] next = increment(model, i, j, forwards); 
		int i = next[0];
		int j = next[1];
			
		if (i>(model.getRows()-1) || i<0)    {
			return false;  // Normal termination
		}
		if (j>(model.getColumns()-1) || j<0) return false;
		
		double x = minX + j * xStep;
		double y = minY + i * yStep;
		if (!gen.containsPoint(x, y)) {
			this.i = i;
			this.j = j;
			this.forwards = next[2]==1;
			return hasNext();
		}

		return true;
	}


	private static final int[] increment(GridModel model, int i, int j, boolean forwards) {
		
		if (model.isSnake()) {
			if (forwards) {
				j = j+1;
				if (j>(model.getColumns()-1)) {
					i++;
					forwards = !forwards;
				}
			} else {
				j = j-1;
				if (j<0) {
					j=0;
					i++;
					forwards = !forwards;
				}
			}

		} else {
			j++;
			if (j>(model.getColumns()-1)) {
				j=0;
				i++;
			}
		}
		return new int[]{i,j, forwards?1:0}; // Bit slow because makes array object to return int values
	}

	
	@Override
	public Point next() {
		
		int[] next = increment(model, i, j, forwards);
		this.i = next[0];
		this.j = next[1];
		this.forwards = next[2]==1;
		
		if (i>(model.getRows()-1) || i<0)    return null;  // Normal termination
		if (j>(model.getColumns()-1) || j<0) throw new NullPointerException("Unexpected index. The j index was "+j);

		double x = minX + j * xStep;
		double y = minY + i * yStep;
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
