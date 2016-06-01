package org.eclipse.scanning.api.scan.rank;

/**
 * 
 * Information about the scan rank of the nexy slice.
 * 
 * @author Matthew Gerring
 *
 */
public interface IScanSlice {
	
	int[] getStart();
	
	int[] getStop();
	
	int[] getStep();
	
}
