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
	
	private IQueueProcessor<? extends Queueable> processor;
	private boolean blocking = true, executed = false, terminated = false;
	
	public QueueProcess(T bean, IPublisher<T> publisher, boolean blocking) {
		super(bean, publisher);
		this.blocking = blocking; //TODO
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
		setTerminated();
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
	public void childQueueBroadcast() throws EventException {
		if (publisher != null) {
			publisher.broadcast(bean);
		}
	}

	@Override
	public IQueueProcessor<? extends Queueable> getProcessor() {
		return processor;
	}

	@Override
	public void setProcessor(IQueueProcessor<? extends Queueable> processor) throws EventException {
		if (isExecuted()) throw new EventException("Cannot chance processor after execution started");
		//This should stop bean type mismatches. A second catch should be included in the execute() of the IQueueProcessor
		if (!(bean.getClass().equals(processor.getBeanClass()))) throw new EventException("Cannot set processor - incorrect bean type");
		this.processor = processor;
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

	public boolean isBlocking() {
		return blocking;
	}

	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}

}
