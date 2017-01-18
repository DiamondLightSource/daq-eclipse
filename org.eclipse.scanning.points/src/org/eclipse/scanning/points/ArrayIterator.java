package org.eclipse.scanning.points;

import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.ArrayModel;
import org.eclipse.scanning.points.ScanPointGeneratorFactory.JythonObjectFactory;
import org.python.core.PyList;

class ArrayIterator extends AbstractScanPointIterator {

	private ArrayModel model;
	int index = 0;

	public ArrayIterator(ArrayGenerator gen) {
		this.model= gen.getModel();
		
        JythonObjectFactory arrayGeneratorFactory = ScanPointGeneratorFactory.JArrayGeneratorFactory();

        double[] points = model.getPositions();        
        PyList names =  new PyList(Arrays.asList(new String[] { model.getName()}));
        
        @SuppressWarnings("unchecked")
		Iterator<IPosition> iterator = (Iterator<IPosition>) arrayGeneratorFactory.createObject(
				names, "mm", points);
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
