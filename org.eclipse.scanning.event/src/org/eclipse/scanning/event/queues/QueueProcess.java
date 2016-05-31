package org.eclipse.scanning.event.queues;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.AbstractPausableProcess;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.IQueueProcess;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;

/**
 * Generic class for processing a queue item, irrespective of its concrete 
 * type. The concrete type should be identified by the 
 * {@link QueueProcessCreator} and  this class then instantiated with the 
 * {@link IQueueProcessor} associated with that type. This class uses 
 * {@link AbstractPausableProcess} to provide generic pause, resume & terminate
 * functions, with bean specific methods called on the processors.
 * 
 * @author Michael Wharmby
 *
 * @param <T> Type acted on by queue consumer (this will probably be a 
 * super-type of the actual bean class). 
 */
public class QueueProcess<T extends Queueable> extends AbstractPausableProcess<T> implements IQueueProcess<T> {
	
	private final IQueueProcessor<T, ? extends Queueable> processor;
	private boolean blocking = true;
	
	public QueueProcess(T bean, IPublisher<T> publisher, boolean blocking, 
			IQueueProcessor<T, ? extends Queueable> processor) {
		super(bean, publisher);
		this.blocking = blocking; //TODO
		this.processor = processor;
	}

	/*
	 * The following methods (doPause, doResume and doTerminate) are called by 
	 * the pause, resume and terminate methods of AbstractPausableProcess 
	 * (which override the default methods of IQueueProcess API). 
	 * e.g. @see org.eclipse.scanning.api.event.core.AbstractPausableProcess#doTerminate()
	 */
	@Override
	public void doPause() throws EventException {
		processor.pause();
	}
	
	@Override
	public void doResume() throws Exception {
		processor.resume();
	}
	
	@Override
	public void doTerminate() throws EventException {
		processor.terminate();
	}
	
	@Override
	public void broadcast(Status newStatus, Double newPercent, String newMessage) throws EventException {
		if (publisher != null && processor != null) {
			
			if (newStatus != null) {
				bean.setPreviousStatus(bean.getStatus());
				bean.setStatus(newStatus);
			}
			if (newPercent != null) bean.setPercentComplete(newPercent);
			if (newMessage != null) bean.setMessage(newMessage);
			
			publisher.broadcast(bean);
		}		
	}

	@Override
	public IQueueProcessor<T, ? extends Queueable> getProcessor() {
		return processor;
	}

	public boolean isBlocking() {
		return blocking;
	}

	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}

}
