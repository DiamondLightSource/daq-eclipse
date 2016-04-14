package org.eclipse.scanning.test.event.queues.mocks;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyProcessor<T extends Queueable> implements IConsumerProcess<T> {
	
	private static final Logger logger = LoggerFactory.getLogger(DummyProcessor.class);
	
	private final T bean;
	private final IPublisher<T> statusReporter;
	private boolean blocking;
	
	private boolean terminated;

	public DummyProcessor(T bean, IPublisher<T> statusNotifier, boolean blocking) {
		this.bean = bean;
		this.statusReporter = statusNotifier;
		this.blocking = blocking;
	}
	
	@Override
	public T getBean() {
		return bean;
	}

	@Override
	public IPublisher<T> getPublisher() {
		return statusReporter;
	}

	@Override
	public void execute() throws EventException {
		if (isBlocking()) {
			run(); // Block until process has run.
		} else {
			final Thread thread = new Thread(new Runnable() {
				public void run() {
					try {
						DummyProcessor.this.run();
					} catch (EventException ne) {
						ne.printStackTrace(); // Only a test process!
					}
				}
			});
			thread.setDaemon(true);
			thread.setPriority(Thread.MAX_PRIORITY);
			thread.start();
		}
		
	}

	@Override
	public void terminate() throws EventException {
		terminated = true;
		
	}
	
	private void run() throws EventException {
		bean.setStatus(Status.RUNNING);
		bean.setPercentComplete(0);
		statusReporter.broadcast(bean);
		
		terminated = false;
		
		for (int i = 0; i < 100; i++) {
			
			if (isTerminated()) {
				bean.setStatus(Status.TERMINATED);
				statusReporter.broadcast(bean);
				return;
			}
			
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				logger.error("Dummy process sleeping failed", e);
			}
			System.out.println("DummyProcessor ("+bean.getClass().getSimpleName()+" - "+bean.getName()+"): "+bean.getPercentComplete());
			bean.setPercentComplete(i);
			statusReporter.broadcast(bean);
		}

		bean.setStatus(Status.COMPLETE);
		bean.setPercentComplete(100);
		bean.setMessage("Dummy process complete (no software run)");
		statusReporter.broadcast(bean);
	}

	public boolean isBlocking() {
		return blocking;
	}

	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}

	public boolean isTerminated() {
		return terminated;
	}

	public void setTerminated(boolean terminated) {
		this.terminated = terminated;
	}

}
