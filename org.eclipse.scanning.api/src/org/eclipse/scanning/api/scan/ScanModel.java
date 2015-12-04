package org.eclipse.scanning.api.scan;

import java.util.Collection;

import org.eclipse.scanning.api.points.IPosition;

public class ScanModel {
	
	/**
	 * Normally this is a generator for the scan points
	 * of the scan. IGenerator implements Iterable
	 */
	private Iterable<IPosition> positionIterator;
	
	/**
	 * This is the set of detectors which should be collected
	 * and (if they are IReadableDetector) read out during the 
	 * scan. The detectors should be configured and ready to 
	 * have run called. For instance IMalcolmDevice should be
	 * configured and have a State from which it can be run,
	 * ideally READY.
	 */
	private Collection<IRunnableDevice<?>> scanners;
	
	public ScanModel() {
	    this(null);
	}
	
	public ScanModel(Iterable<IPosition> positionIterator) {
		this.positionIterator = positionIterator;
	}

	public Iterable<IPosition> getPositionIterator() {
		return positionIterator;
	}

	public void setPositionIterator(Iterable<IPosition> positionIterator) {
		this.positionIterator = positionIterator;
	}

	public Collection<IRunnableDevice<?>> getScanners() {
		return scanners;
	}

	public void setScanners(Collection<IRunnableDevice<?>> scanners) {
		this.scanners = scanners;
	}
}
