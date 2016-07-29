package org.eclipse.scanning.example.scannable;

import org.eclipse.scanning.api.points.IPosition;

/**
 * Designed to model exiting the scan because a certain value is illegal
 * 
 * @author Matthew Gerring
 *
 */
public class MockBeanOnMonitor extends MockScannable {

	public MockBeanOnMonitor(String string, double d, int i) {
		super(string,d,i);
	}

	public void setPosition(Number position, IPosition loc) throws Exception {
		
		final int step = loc.getStepIndex();
		if (step>0 && step%10==0) { // We wait
			System.out.println("Beam is deamed to be off ");
			throw new Exception("Cannot run scan further!");
		}
		super.setPosition(position, loc);
	}

}
