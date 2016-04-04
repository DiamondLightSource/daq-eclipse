package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.ArrayModel;

public class ArrayGenerator extends AbstractGenerator<ArrayModel, IPosition> {
	
	ArrayGenerator() {
		setLabel("Array Scan");
		setDescription("Creates a scan from an array of positions");
	}

	@Override
	protected void validateModel() {
		// Nothing(?) to validate.
	}

	@Override
	public int sizeOfValidModel() throws GeneratorException {
		if (containers!=null) throw new GeneratorException("Cannot deal with regions in an array scan!");
		if (model.getPositions() == null) {
			return 0;
		}
		return model.getPositions().length;
	}
	
	@Override
	protected Iterator<IPosition> iteratorFromValidModel() {
		return new ArrayIterator(this);
	}

}
