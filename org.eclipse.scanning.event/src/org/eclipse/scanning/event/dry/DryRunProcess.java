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

	public DryRunProcess(StatusBean bean, IPublisher<StatusBean> statusPublisher) {
		this.bean = bean;
		this.publisher = statusPublisher;
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
		
		for (int i = 0; i < 100; i++) {
			
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
		throw new EventException("Cannot terminate dry run!");
	}

}
