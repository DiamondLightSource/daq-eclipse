package org.eclipse.scanning.api.points;

import java.util.List;

/**
 * 
 * Use this interface to return the size of the scan without having to look at all the points.
 * Used whenever the scan positions have to look at hardware to run. Avoids calling
 * the iterator until needed.
 * 
 * 
 * This is a good idea when the iterator checks an moves hardware for each point.
 * Ideally the hardware should all be moved by the scanning and the iterator return the
 * list of precalculated point but in some instances it is desirable to calculate the
 * next position based on where the last position got to. In this case all points
 * cannot be interated until they are run and you must implement this interface to 
 * provide extra scan information such that no points have to be looked at until the scan
 * runs.
 * 
 * @author Matthew Gerring
 *
 */
public interface IDeviceDependentIterable extends Iterable<IPosition> {
	/**
	 * The size of the points iterator without asking for a next call on the iterator. 
	 * This call will be as fast as possible, it may estimate the size, in this case 
	 * its estimation should be >= the actual point iterated.
	 * 
	 * @return
	 */
	int size() throws GeneratorException;

	/**
	 * Provide the names of the scannables at each position without making
	 * an iterator.
	 * 
	 * @return
	 */
	List<String> getScannableNames();

}
