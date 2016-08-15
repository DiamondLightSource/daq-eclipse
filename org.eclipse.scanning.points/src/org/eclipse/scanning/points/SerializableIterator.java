package org.eclipse.scanning.points;

import java.util.Iterator;

import org.python.core.PyDictionary;

public abstract class SerializableIterator<E> implements Iterator<E> {
	
	public PyDictionary toDict() {
		return null;
	}

	public int size() {
		return 0;
	}
}
