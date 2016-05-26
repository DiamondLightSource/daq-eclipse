package org.eclipse.scanning.event.queues;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.AbstractPausableProcess;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;

public class QueueProcess<T extends Queueable> extends AbstractPausableProcess<T> {
	
	private final IQueueProcessor<? extends Queueable> processor;
	private boolean terminated = false, blocking = true;
	
//	//Number of ms processor waits in while loop before checking state of task.
//	protected final long loopSleepTime = 100;
	
	public QueueProcess(T bean, IPublisher<T> publisher, boolean blocking, IQueueProcessor<? extends Queueable> processor) {
		super(bean, publisher);
		this.blocking = blocking;
		this.processor = processor;
	}

	@Override
	public void execute() throws EventException, InterruptedException {
		processor.execute();
	}

	@Override
	public void terminate() throws EventException {
		processor.terminate();
		terminated = true;
	}
	
	/**
	 * Convenience method to call broadcast with only {@link Status} argument.
	 * 
	 * @param bean Bean to be broadcast.
	 * @param newStatus Status the bean has just reached.
	 * @throws EventException In case broadcasting fails.
	 */
	public void broadcast(T bean, Status newStatus) throws EventException {
		broadcast(bean, newStatus, null);
	}
	
	/**
	 * Convenience method to call broadcast with only percent complete 
	 * argument.
	 * 
	 * @param bean Bean to be broadcast.
	 * @param newPercent The value percent complete should be set to.
	 * @throws EventException In case broadcasting fails.
	 */
	public void broadcast(T bean, double newPercent) throws EventException {
		broadcast(bean, null, newPercent);
	}

	/**
	 * Broadcast the new status, updated previous status and percent complete 
	 * of the given bean.
	 * 
	 * @param bean Bean to be broadcast.
	 * @param newStatus Status the bean has just reached.
	 * @param newPercent The value percent complete should be set to.
	 * @throws EventException In case broadcasting fails.
	 */
	public void broadcast(T bean, Status newStatus, Double newPercent) throws EventException {
		if (publisher != null) {
			if (newStatus != null) {
				bean.setPreviousStatus(bean.getStatus());
				bean.setStatus(newStatus);
			}
			if (newPercent != null) bean.setPercentComplete(newPercent);
			
			publisher.broadcast(bean);
		}		
	}

	public IQueueProcessor<? extends Queueable> getProcessor() {
		return processor;
	}

	public boolean isTerminated() {
		return terminated;
	}

	public void setTerminated(boolean terminated) {
		this.terminated = terminated;
	}

	public boolean isBlocking() {
		return blocking;
	}

	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}

}
