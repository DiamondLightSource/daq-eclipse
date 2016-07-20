package org.eclipse.scanning.event.queues.processors;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.IQueueBroadcaster;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.beans.IAtomBeanWithQueue;
import org.eclipse.scanning.api.event.queues.beans.IAtomQueue;
import org.eclipse.scanning.api.event.queues.beans.Queueable;

/**
 * Generic class for processing a {@link Queueable} composed of an 
 * {@link IAtomQueue}. The processor spools the atoms in the contained queue 
 * into a new queue created through the {@link IQueueService}. The new queue is
 * monitored using the {@link QueueListener} and through the queue service.
 * 
 * TODO Implement class!!!
 * TODO Rehash java-doc once implemented
 * TODO Add test of wrong bean type before cast.
 * 
 * @author Michael Wharmby
 *
 * @param <T> Bean implementing {@link Queueable}, but must be an 
 *            {@link IAtomBeanWithQueue}.
 */
public class AtomQueueProcessor implements IQueueProcessor {

	@Override
	public void execute() throws EventException, InterruptedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void terminate() throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Class getBeanClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Queueable getProcessBean() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setProcessBean(Queueable bean) throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IQueueBroadcaster getQueueBroadcaster() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setQueueBroadcaster(IQueueBroadcaster broadcaster) throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isExecuted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setExecuted() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isTerminated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setTerminated() {
		// TODO Auto-generated method stub
		
	}



}
