package org.eclipse.scanning.api.scan;

import java.util.Arrays;
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
	 * scan.
	 */
	private Collection<IRunnableDevice<?>> detectors;
	
	public ScanModel() {
	    this(null);
	}	
	public ScanModel(Iterable<IPosition> positionIterator, IRunnableDevice<?>... detectors) {
		this.positionIterator = positionIterator;
		if (detectors!=null && detectors.length>0) this.detectors = Arrays.asList(detectors);
	}


	public Iterable<IPosition> getPositionIterator() {
		return positionIterator;
	}

	public void setPositionIterator(Iterable<IPosition> positionIterator) {
		this.positionIterator = positionIterator;
	}

	public Collection<IRunnableDevice<?>> getDetectors() {
		return detectors;
	}

	public void setDetectors(Collection<IRunnableDevice<?>> ds) {
		this.detectors = ds;
	}

	public void setDetectors(IRunnableDevice<?>... detectors) {
		this.detectors = Arrays.asList(detectors);
	}
}
