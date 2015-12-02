package org.eclipse.scanning.api.scan;

import org.eclipse.scanning.api.points.IPosition;

public class ScanModel {
	
	/**
	 * Normally this is a generator for the scan points
	 * of the scan. IGenerator implements Iterable
	 */
	private Iterable<IPosition> positionIterator;
	
	/**
	 * The parser from the scan command, may be null
	 * if no scan command was parsed.
	 */
	private IParser<?>          parser;

	public Iterable<IPosition> getPositionIterator() {
		return positionIterator;
	}

	public void setPositionIterator(Iterable<IPosition> positionIterator) {
		this.positionIterator = positionIterator;
	}
}
