package org.eclipse.scanning.test.event.queues.mocks;

import java.util.concurrent.CountDownLatch;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;

public class MockQueueProcessor implements IQueueProcessor {

	private CountDownLatch execLatch;
	private long delay = 500;

	//Metrics for reporting interactions.
	private long runTime = 0;
	private boolean executed;
	private boolean complete;
	private boolean terminated;

	public MockQueueProcessor(CountDownLatch latch) {
		execLatch = latch;
	}

	@Override
	public void execute() throws EventException, InterruptedException {
		executed = true;
		
		long startTime = System.currentTimeMillis();
		runTime = System.currentTimeMillis() - startTime;
		while (runTime < delay) {
			Thread.sleep(10);
			runTime = System.currentTimeMillis() - startTime;
			if (terminated) {
				execLatch.countDown();
				return;
			}
		}
		complete = true;
		execLatch.countDown();
	}

	@Override
	public void terminate() throws EventException {
		terminated = true;
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public long getRunTime() {
		return runTime;
	}

	public boolean isExecuted() {
		return executed;
	}

	public boolean isComplete() {
		return complete;
	}

	public boolean isTerminated() {
		return terminated;
	}

}
