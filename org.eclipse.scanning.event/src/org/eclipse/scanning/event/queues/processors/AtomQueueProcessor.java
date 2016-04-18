package org.eclipse.scanning.event.queues.processors;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IPublisher;
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
	public <T extends Queueable> IConsumerProcess<T> makeProcess(T bean,
			IPublisher<T> publisher, boolean blocking) {
		return new AtomQueueProcess<T>(bean, publisher, blocking);
	}

	class AtomQueueProcess <T extends Queueable> extends AbstractQueueProcessor<T> {

		public AtomQueueProcess(T bean, IPublisher<T> publisher, boolean blocking) {
			super(bean, publisher);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void execute() throws EventException {
			// TODO Auto-generated method stub

		}

		@Override
		public void terminate() throws EventException {
			// TODO Auto-generated method stub

		}

	}

}
