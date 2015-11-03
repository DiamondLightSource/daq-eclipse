package org.eclipse.scanning.event.dry;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DryRunProcess implements IConsumerProcess<StatusBean> {
	
	private static final Logger logger = LoggerFactory.getLogger(DryRunProcess.class);

	private final StatusBean             bean;
	private final IPublisher<StatusBean> publisher;
	private boolean                      blocking;
	
	private boolean terminated;

	public DryRunProcess(StatusBean bean, IPublisher<StatusBean> statusPublisher, boolean blocking) {
		this.bean = bean;
		this.publisher = statusPublisher;
		this.blocking = blocking;
	}

	@Override
	public StatusBean getBean() {
		return bean;
	}

	@Override
	public IPublisher<StatusBean> getPublisher() {
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
		
		terminated = false;
		for (int i = 0; i < 100; i++) {
			
			if (isTerminated()) {
				bean.setStatus(Status.TERMINATED);
				publisher.broadcast(bean);
				return;
			}
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				logger.error("Dry run sleeping failed", e);
			}
			System.out.println("Dry run : "+bean.getPercentComplete());
			bean.setPercentComplete(i);
			publisher.broadcast(bean);
		}

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
