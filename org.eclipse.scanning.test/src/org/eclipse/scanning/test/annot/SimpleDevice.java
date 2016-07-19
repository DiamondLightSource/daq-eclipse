package org.eclipse.scanning.test.annot;

import org.eclipse.scanning.api.annotation.scan.ScanStart;

/**
 * 
 * Could use Mockito but always causes compilation issues
 *
 */
public class SimpleDevice {

	private int count=0;
	
	@ScanStart
	public void start() throws Exception {
		count++;
	}
	
	public int getCount() {
		return count;
	}
}