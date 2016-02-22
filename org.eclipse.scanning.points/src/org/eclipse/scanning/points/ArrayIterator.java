package org.eclipse.scanning.points;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.points.models.ArrayModel;

class ArrayIterator implements Iterator<IPosition> {

	private ArrayModel model;
	int index = 0;

	public ArrayIterator(ArrayGenerator gen) {
		this.model= gen.getModel();
	}

	@Override
	public boolean hasNext() {
		if (model.getPositions() == null) {
			return false;
		}
		return index < model.getPositions().length;
	}

	@Override
	public IPosition next() {
		if (model.getPositions() != null && index < model.getPositions().length) {
			return new Scalar(model.getName(), index, model.getPositions()[index++]);
		}
		throw new NoSuchElementException();
	}

	public void remove() {
		throw new UnsupportedOperationException("remove");
	}
}
