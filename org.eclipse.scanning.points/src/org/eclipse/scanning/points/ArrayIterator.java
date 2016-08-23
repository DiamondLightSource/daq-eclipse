package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.ArrayModel;
import org.eclipse.scanning.points.ScanPointGeneratorFactory.JythonObjectFactory;

class ArrayIterator extends AbstractScanPointIterator {

	private ArrayModel model;
	int index = 0;

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
	}

	@Override
	public IPosition next() {
		return pyIterator.next();
	}

	public void remove() {
		throw new UnsupportedOperationException("remove");
	}
}
