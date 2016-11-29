package org.eclipse.scanning.example.scannable;

import org.eclipse.scanning.api.event.core.IDisconnectable;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Scalar;

/**
 * Designed to monitor topup (pretty badly, just conceptually). 
 * On a step divisible by ten, will force a wait
 * until imaginary topup value is reached.
 * 
 * @author Matthew Gerring
 *
 */
public class MockTopupScannable extends MockScannable implements IDisconnectable {

	private final long start;
	private long period;
    private volatile boolean isRunning;
	/**
	 * 
	 * @param name
	 * @param period in ms that topup happens over e.g. 5000 for testing
	 */
	public MockTopupScannable(String name, long period) {
		super(name, System.currentTimeMillis());
		setUnit("ms");
		start = System.currentTimeMillis();
		this.period = period;
		final Thread thread = new Thread(()->{
			try {
				isRunning = true;
				while(isRunning) {
				    delegate.firePositionChanged(getLevel(), new Scalar(getName(), -1, getPosition()));
				    Thread.sleep(100);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, "Topup value thread");
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY+1);
		thread.start();
	}
	
	@Override
	public void disconnect() {
		isRunning = false;
	}
	@Override
	public boolean isDisconnected() {
		return !isRunning;
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
