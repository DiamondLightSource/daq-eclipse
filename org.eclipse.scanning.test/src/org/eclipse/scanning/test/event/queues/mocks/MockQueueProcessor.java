package org.eclipse.scanning.test.event.queues.mocks;

import java.util.concurrent.CountDownLatch;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.IQueueBroadcaster;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.beans.Queueable;

public class MockQueueProcessor <T extends Queueable> implements IQueueProcessor<T> {

	private final CountDownLatch execLatch;
	private int counter = 500;
	
	private final T bean;
	
	private IQueueBroadcaster<? extends Queueable> broadcaster;

	//Metrics for reporting interactions.
	private long runTime = 0;
	private boolean executed;
	private boolean complete;
	private boolean terminated;

	public MockQueueProcessor(T bean, CountDownLatch latch) {
		execLatch = latch;
		this.bean = bean;
	}
	
	public MockQueueProcessor(IQueueBroadcaster<? extends Queueable> broadcaster, T bean, CountDownLatch latch) {
		this(bean, latch);
		this.broadcaster = broadcaster;
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
		setComplete();
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

	@SuppressWarnings("unchecked")
	@Override
	public Class<T> getBeanClass() {
		return (Class<T>) bean.getClass();
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
	public void setQueueBroadcaster(IQueueBroadcaster<? extends Queueable> process) {
		// TODO Auto-generated method stub
		
	}

	@SuppressWarnings("hiding")
	@Override
	public <T extends Queueable> void setProcessBean(T bean) throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setExecuted() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTerminated() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public T getProcessBean() {
		return bean;
	}

	@Override
	public IQueueBroadcaster<? extends Queueable> getQueueBroadcaster() {
		return broadcaster;
	}

	@Override
	public void setComplete() {
		complete = true;
		execLatch.countDown();
	}

}
