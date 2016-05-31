package org.eclipse.scanning.test.event.queues.mocks;

import java.util.concurrent.CountDownLatch;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.IQueueProcess;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.test.event.queues.dummy.DummyBean;

public class MockQueueProcessor <T extends Queueable> implements IQueueProcessor<T, DummyBean> {

	private CountDownLatch execLatch;
	private int counter = 500;

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
		while (counter > 0) {
			Thread.sleep(10);
			counter -= 10;
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

	public long getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
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

	@Override
	public Class<DummyBean> getBeanClass() {
		return DummyBean.class;
	}

	@Override
	public void pause() throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IQueueProcess<T> getProcess() {
		// TODO Auto-generated method stub
		return null;
	}

}
