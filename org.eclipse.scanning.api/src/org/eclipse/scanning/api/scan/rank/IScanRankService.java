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
public interface IScanRankService {

	/**
	 * 
	 * @param context
	 * @param position
	 * @param datashape
	 * @return
	 */
	IScanSlice createScanSlice(IPosition position, int... datashape);
	
	/**
	 * Currently it is possible to implemnent the IScanRankService
	 * directly in the API bundle. It may be necessary in future to 
	 * move this to a bundle with dependencies. In this case this static
	 * method will pick up the implementation of the service via 
	 * osgi services.
	 * 
	 * @return
	 */
    public static IScanRankService getScanRankService() {
    	return scanRankService;
    }
    
    /**
     * Do not use this variable, use getScanRankService();
     */
	static final IScanRankService scanRankService = new ScanRankService();
}
