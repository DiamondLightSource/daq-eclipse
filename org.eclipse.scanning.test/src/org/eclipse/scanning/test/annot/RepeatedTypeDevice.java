package org.eclipse.scanning.test.annot;

import org.eclipse.scanning.api.annotation.scan.PointEnd;
import org.eclipse.scanning.api.annotation.scan.PointStart;

public class RepeatedTypeDevice {

	@PointStart
	public void repeatedTypes1(String a, String b) {
		
	}
	
	@PointEnd
	public void repeatedTypes2(int a, int b) {
		
	}

}
