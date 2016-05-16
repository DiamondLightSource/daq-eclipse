package org.eclipse.scanning.event.queues;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.event.queues.processors.AbstractQueueProcessor;

public class QueueProcess<T extends Queueable> extends AbstractQueueProcessor<T> {
	
	private boolean blocking;
	private IQueueProcessor processor;

	public QueueProcess(T bean, IPublisher<T> publisher, boolean blocking, IQueueProcessor processor) {
		super(bean, publisher);
		this.blocking = blocking;
		this.processor = processor;
	}

	@Override
	public void execute() throws EventException, InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void terminate() throws EventException {
		// TODO Auto-generated method stub

	}

	public boolean isBlocking() {
		return blocking;
	}

	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}

	public IQueueProcessor getProcessor() {
		return processor;
	}

	public void setProcessor(IQueueProcessor processor) {
		this.processor = processor;
	}
	
	

}
