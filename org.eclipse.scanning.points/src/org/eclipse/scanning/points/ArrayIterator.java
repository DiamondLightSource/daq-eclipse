package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.points.models.ArrayModel;
import org.eclipse.scanning.points.ScanPointGeneratorFactory.JythonObjectFactory;

class ArrayIterator implements Iterator<IPosition> {

	private ArrayModel model;
	int index = 0;
	
	private Iterator<IPosition> pyIterator;

	public ArrayIterator(ArrayGenerator gen) {
		this.model= gen.getModel();
		
        JythonObjectFactory arrayGeneratorFactory = ScanPointGeneratorFactory.JArrayGeneratorFactory();

        String name = model.getName();
        double[] points = model.getPositions();
        
        @SuppressWarnings("unchecked")
		Iterator<IPosition> iterator = (Iterator<IPosition>) arrayGeneratorFactory.createObject(
				name, "mm", points);
        pyIterator = iterator;
	}

	@Override
	public boolean hasNext() {
		return pyIterator.hasNext();
		
//		if (model.getPositions() == null) {
//			return false;
//		}
//		return index < model.getPositions().length;
	}

	@Override
	public IPosition next() {
		return pyIterator.next();
		
//		if (model.getPositions() != null && index < model.getPositions().length) {
//			return new Scalar(model.getName(), index, model.getPositions()[index++]);
//		}
//		throw new NoSuchElementException();
	}

	public void remove() {
		throw new UnsupportedOperationException("remove");
	}
}
