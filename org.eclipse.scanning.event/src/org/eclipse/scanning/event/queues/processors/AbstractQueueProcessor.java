package org.eclipse.scanning.event.queues.processors;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.IQueueProcess;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.beans.Queueable;

public abstract class AbstractQueueProcessor <P extends Queueable> implements IQueueProcessor<P> {
	
	private boolean terminated = false, executed = false;
	
	protected P bean;
	protected IQueueProcess<? extends Queueable> process;

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Queueable> void setProcessBean(T bean) throws EventException {
		if (isExecuted()) throw new EventException("Cannot change queueProcess after execution started.");
		if (bean.getClass().equals(getBeanClass())) {
			this.bean = (P)bean;
		} else {
			throw new EventException("Unsupported bean type");
		}
	}

	@Override
	public void setQueueProcess(IQueueProcess<? extends Queueable> process) throws EventException {
		if (isExecuted()) throw new EventException("Cannot change queueProcess after execution started.");
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

}
