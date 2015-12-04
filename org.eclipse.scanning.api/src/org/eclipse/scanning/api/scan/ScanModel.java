package org.eclipse.scanning.api.scan;

import org.eclipse.scanning.api.points.IPosition;

public class ScanModel {
	
	/**
	 * The start location for the scan.
	 */
	private IPosition start;
	
	/**
	 * Normally this is a generator for the scan points
	 * of the scan. IGenerator implements Iterable
	 */
	private Iterable<IPosition> positionIterator;
	
	public ScanModel() {
	    this(null);
	}
	
	public ScanModel(Iterable<IPosition> positionIterator) {
		this.positionIterator = positionIterator;
	}
	
	public ScanModel(Iterable<IPosition> positionIterator, String detectorName, double exposure) {
		this.positionIterator = positionIterator;
	}

	public Iterable<IPosition> getPositionIterator() {
		return positionIterator;
	}

	public void setPositionIterator(Iterable<IPosition> positionIterator) {
		this.positionIterator = positionIterator;
	}

	public IPosition getStart() {
		return start;
	}

	public void setStart(IPosition start) {
		this.start = start;
	}
}
