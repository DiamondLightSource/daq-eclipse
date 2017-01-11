package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.points.models.RepeatedPointModel;

class RepeatedPointIterator implements Iterator<IPosition> {

	private RepeatedPointModel   model;
	private int count = 0;
	
	public RepeatedPointIterator(RepeatedPointGenerator gen) {
		this.model= gen.getModel();
	}

	@Override
	public boolean hasNext() {
		return count<model.getCount();
	}

	@Override
	public IPosition next() {
		
		if (model.getSleep()>0) {
			try {
				Thread.sleep(model.getSleep());
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		Scalar<Double> point = new Scalar<>(model.getName(), count, model.getValue());
		count++;
		return point;
	}

	public void remove() {
        throw new UnsupportedOperationException("remove");
    }

}
