package org.eclipse.scanning.example.scannable;

import org.eclipse.scanning.api.points.IPosition;

/**
 * Designed to monitor topup (pretty badly, just conceptually). 
 * On a step divisible by ten, will force a wait
 * until imaginary topup value is reached.
 * 
 * @author Matthew Gerring
 *
 */
public class MockTopupScannable extends MockScannable {

	private final long start;
	private long period;

	/**
	 * 
	 * @param name
	 * @param period in ms that topup happens over e.g. 5000 for testing
	 */
	public MockTopupScannable(String name, long period) {
		super(name, System.currentTimeMillis());
		start = System.currentTimeMillis();
		this.period = period;
	}

	public void setPosition(Number position, IPosition loc) throws Exception {
		
	}
	
	/**
	 * Time in ms until next topup
	 */
	@Override
	public Number getPosition() {
		long diff = System.currentTimeMillis() - start;
		long timems = period-(diff%period);
		return timems;
	}

	public long getPeriod() {
		return period;
	}

	public void setPeriod(long period) {
		this.period = period;
	}
}
