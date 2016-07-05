package org.eclipse.scanning.test.scan.mock;

import org.eclipse.scanning.api.points.IPosition;

/**
 * Designed to monitor topup (pretty badly, just conceptually). 
 * On a step divisible by ten, will force a wait
 * until imaginary topup value is reached.
 * 
 * @author Matthew Gerring
 *
 */
public class MockTopupMonitor extends MockScannable {

	public MockTopupMonitor(String string, double d, int i) {
		super(string,d,i);
	}

	public void setPosition(Number position, IPosition loc) throws Exception {
		
		final int step = loc.getStepIndex();
		if (step%10==0) { // We wait
			System.out.println("Waiting for imaginary topup for 10ms ");
			Thread.sleep(10);
			System.out.println("Bean current is now stable again... ");
		}
		super.setPosition(position, loc);
	}

}
