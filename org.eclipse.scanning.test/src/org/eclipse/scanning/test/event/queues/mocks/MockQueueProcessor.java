package org.eclipse.scanning.test.event.queues.mocks;

import java.util.concurrent.CountDownLatch;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.beans.Queueable;

public class MockQueueProcessor implements IQueueProcessor {
	
	private CountDownLatch execLatch;
	private long delay;
	
	//Metrics for reporting interactions.
	private long runTime = 0;
	private boolean isExecuted;
	private boolean isComplete;
	private boolean isTerminated;
	
	public MockQueueProcessor(CountDownLatch latch) {
		execLatch = latch;
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
		return isExecuted;
	}

	public boolean isComplete() {
		return isComplete;
	}

	public boolean isTerminated() {
		return isTerminated;
	}

}
