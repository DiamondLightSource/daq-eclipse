package org.eclipse.scanning.event.queues.processors;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.IQueueProcess;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractQueueProcessor <P extends Queueable> implements IQueueProcessor<P> {
	
	private static Logger logger = LoggerFactory.getLogger(AbstractQueueProcessor.class);
	
	private boolean terminated = false, executed = false, complete = false;
	
	protected P bean;
	protected IQueueProcess<? extends Queueable> process;

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Queueable> void setProcessBean(T bean) throws EventException {
		if (isExecuted()) {
			logger.error("Cannot change bean to be processed after execution has started.");
			throw new EventException("Cannot change bean to be processed after execution has started");
		}
		if (bean.getClass().equals(getBeanClass())) {
			this.bean = (P)bean;
		} else {
			logger.error("Cannot set bean: Bean type "+bean.getClass().getSimpleName()+" not supported by "+getClass().getSimpleName()+".");
			throw new EventException("Unsupported bean type");
		}
	}

	@Override
	public void setQueueProcess(IQueueProcess<? extends Queueable> process) throws EventException {
		if (isExecuted()) {
			logger.error("Cannot change IQueueProcess after execution has started.");
			throw new EventException("Cannot change IQueueProcess after execution has started");
		}
		this.process = process;
	}

	@Override
	public boolean isExecuted() {
		return executed;
	}

	@Override
	public void setExecuted() {
		executed = true;
	}

	@Override
	public boolean isTerminated() {
		return terminated;
	}

	@Override
	public void setTerminated() {
		terminated = true;
	}
	
	public boolean isComplete() {
		return complete;
	}
	public void setComplete() {
		complete = true;
	}

}
