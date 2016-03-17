package org.eclipse.scanning.event.dry;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DryRunProcess<T extends StatusBean> implements IConsumerProcess<T> {
	
	private static final Logger logger = LoggerFactory.getLogger(DryRunProcess.class);

	private final T                      bean;
	private final IPublisher<T>          publisher;
	private boolean                      blocking;
	
	private boolean terminated;

	private int stop;
	private int start;
	private int step;
	private long sleep;

	public DryRunProcess(T bean, IPublisher<T> statusPublisher, boolean blocking) {
		this(bean,statusPublisher,blocking,0,100,1,100);
	}
	public DryRunProcess(T bean, IPublisher<T> statusPublisher, boolean blocking, int start, int stop, int step, long sleep) {
		this.bean      = bean;
		this.publisher = statusPublisher;
		this.blocking  = blocking;
		this.start = start;
		this.stop  = stop;
		this.step  = step;
		this.sleep  = sleep;
	}

	@Override
	public T getBean() {
		return bean;
	}

	@Override
	public IPublisher<T> getPublisher() {
		return publisher;
	}

	@Override
	public void execute() throws EventException {
		if (isBlocking()) {
			run(); // Block until process has run.
		} else {
			final Thread thread = new Thread(new Runnable() {
				public void run() {
					try {
						DryRunProcess.this.run();
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
	
	private void run()  throws EventException {
		
		bean.setPreviousStatus(Status.QUEUED);
		bean.setStatus(Status.RUNNING);
		bean.setPercentComplete(0d);
		publisher.broadcast(bean);

		terminated = false;
		for (int i = start; i <= stop; i+=step) {
			
			if (isTerminated()) {
				bean.setPreviousStatus(Status.RUNNING);
				bean.setStatus(Status.TERMINATED);
				publisher.broadcast(bean);
				return;
			}
			
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				logger.error("Dry run sleeping failed", e);
			}
			System.out.println("Dry run : "+bean.getPercentComplete()+" : "+bean.getName());
			bean.setPercentComplete((Double.valueOf(i)/Double.valueOf(stop))*100d);
			publisher.broadcast(bean);
		}

		bean.setPreviousStatus(Status.RUNNING);
		bean.setStatus(Status.COMPLETE);
		bean.setPercentComplete(100);
		bean.setMessage("Dry run complete (no software run)");
		publisher.broadcast(bean);
	}

	@Override
	public void terminate() throws EventException {
		terminated = true;
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
