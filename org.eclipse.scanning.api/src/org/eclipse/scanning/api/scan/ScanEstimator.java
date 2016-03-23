package org.eclipse.scanning.api.scan;

import java.util.Iterator;

import org.eclipse.scanning.api.points.IPosition;

/**
 * 
 * Most scans are static and therefore they can have their shape and size
 * discovered. Those scans which truely are iterators and on the fly decide
 * the next position, can only have their shapes and sizes estimated.
 * 
 * Shape is more expensive to estimate that size. For 10 million points size
 * is ~100ms depending on iterator type. Shape will take longer as more floating 
 * point operations are required.
 * 
 * @author Matthew Gerring
 *
 */
public class ScanEstimator {

	private int   size;
	private int[] shape;

	/**
	 * This constructor run the loop over the points in order to
	 * estimate shape and size.
	 * 
	 * @param iterable
	 * @param requireShape Do not set require shape unless you do because it is expensive for huge scans.
	 */
	public ScanEstimator(Iterable<? extends IPosition> iterable, boolean requireShape) {
		
		this.size  = 0;
		this.shape = null;
		for (Iterator<? extends IPosition> it = iterable.iterator(); it.hasNext();) {
			IPosition pos = it.next();
			size++; // Fast even for large stuff
			
			// Not so fast, can turn off for large scans.
			if (requireShape) {
				if (shape==null) shape = new int[pos.getNames().size()];
				
				// TODO Inefficient for large stuff - is this right?
				// We have to do this for every point because they do 
				// not have to iterate in order.
				int i = 0;
				for (String name : pos.getNames()) {
					shape[i] = Math.max(shape[i], pos.getIndex(name)+1);
					++i;
				}
			}
		}
	}

	public int getSize() {
		return size;
	}

	public int[] getShape() {
		return shape;
	}
}
