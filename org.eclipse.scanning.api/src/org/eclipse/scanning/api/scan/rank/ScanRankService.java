package org.eclipse.scanning.api.scan.rank;

import org.eclipse.scanning.api.points.IPosition;

/**
 * 
 * Please use this class to figure out the correct slice
 * from a position in a scan.
 * 
 * @author Matthew Gerring
 *
 */
class ScanRankService implements IScanRankService {

	
	public IScanSlice createScanSlice(IPosition position, int... datashape) {
		
       
		final int scanRank = position.getScanRank();
		final int[] start = new int[scanRank+datashape.length];
		final int[] stop  = new int[scanRank+datashape.length];
		
		for (int dim = 0; dim < scanRank; dim++) {
			start[dim] = position.getIndex(dim);
			stop[dim]  = position.getIndex(dim)+1;
		}

		int index = 0;
		for (int i = datashape.length; i>0; i--) {
			start[start.length-i] = 0;
			stop[stop.length-i]  = datashape[index];
			index++;
		}
	  
		return new ScanSlice(start, stop, null);
	}

}
