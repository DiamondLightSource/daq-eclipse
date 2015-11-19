package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.IPointContainer;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.RasterModel;

public class RasterIterator implements Iterator<Point> {

	private RasterModel        model;
	private IPointContainer<?> container;
	
	public RasterIterator(RasterModel model, IPointContainer<?> container) {
		this.model     = model;
		this.container = container;
	}

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Point next() {
		// TODO Auto-generated method stub
		return null;
	}

}
