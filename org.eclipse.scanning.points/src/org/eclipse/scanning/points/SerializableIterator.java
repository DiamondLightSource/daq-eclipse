package org.eclipse.scanning.points;

import java.util.Iterator;

public interface SerializableIterator<E> extends Iterator<E>, PySerializable {	
	int size();
}
