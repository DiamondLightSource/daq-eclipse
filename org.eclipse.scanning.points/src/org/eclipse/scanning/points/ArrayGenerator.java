package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.ArrayModel;

public class ArrayGenerator extends AbstractGenerator<ArrayModel, IPosition> {
	
	ArrayGenerator() {
		setLabel("Position List Scan");
		setDescription("Creates a scan from a list of positions");
	}

	@Override
	public int size() throws GeneratorException {
		if (containers!=null) throw new GeneratorException("Cannot deal with regions in a position list scan!");
		if (model.getPositions() == null) {
			return 0;
		}
		return model.getPositions().length;
	}
	
	@Override
	public Iterator<IPosition> iterator() {
		return new ArrayIterator(this);
	}

}
