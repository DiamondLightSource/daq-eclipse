package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.IPosition;

public abstract class AbstractScanPointIterator implements Iterator<IPosition> {
	
	protected Iterator<IPosition> pyIterator;

	public Iterator<IPosition> getPyIterator() {
		return pyIterator;
	}

	public void setPyIterator(Iterator<IPosition> pyIterator) {
		this.pyIterator = pyIterator;
	}
	
}
