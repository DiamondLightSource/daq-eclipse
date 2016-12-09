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
	private Thread thread;
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
	}
	
	public void start() {
		
		if (thread!=null && isRunning) return; // We have one going.
		this.thread = new Thread(()->{
			isRunning = true;
			try {
				while(isRunning && !Thread.interrupted()) {
				    delegate.firePositionChanged(getLevel(), new Scalar(getName(), -1, getPosition()));
				    Thread.sleep(100);
				}
			} catch (InterruptedException e) {
				return; // Normal
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				isRunning = false;
			}
		}, "Topup value thread");
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY+1);
		thread.start();
	}
	
	@Override
	public void disconnect() {
		isRunning = false;
		if (thread!=null) thread.interrupt();
	}
	@Override
	public boolean isDisconnected() {
		return !isRunning;
	}

	public void setPosition(Number position) throws Exception {
		setPosition(position, null);
	}
	public void setPosition(Number position, IPosition loc) throws Exception {
	    delegate.firePositionChanged(getLevel(), new Scalar(getName(), -1, position));
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
