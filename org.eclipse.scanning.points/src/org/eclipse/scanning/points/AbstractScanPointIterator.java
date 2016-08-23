package org.eclipse.scanning.points;

import java.util.Iterator;

import org.eclipse.scanning.api.points.IPosition;
import org.python.core.PyDictionary;

public abstract class AbstractScanPointIterator implements SerializableIterator<IPosition>{

	protected Iterator<IPosition> pyIterator;

	public Iterator<IPosition> getPyIterator() {
		return pyIterator;
	}

	public void setPyIterator(Iterator<IPosition> pyIterator) {
		this.pyIterator = pyIterator;
	}

	public PyDictionary toDict() {
		return null;
	}
	
	public int size() {
		return 0;
	}
}
