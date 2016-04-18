package org.eclipse.scanning.event.queues.processors;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.event.queues.beans.MonitorAtom;

/**
 * MonitorAtomProcessor reads back a single value from a monitor. It will use 
 * the view detector methods discussed that should be available as part of the
 * Mapping project. TODO!!!!
 * 
 * TODO Implement class!!!
 * TODO Rehash java-doc once implemented
 * TODO Add test of wrong bean type before cast.
 * 
 * @author Michael Wharmby
 *
 * @param <T> Bean implementing {@link Queueable}, but must be a 
 *            {@link MonitorAtom}.
 */
public class MonitorAtomProcessor implements IQueueProcessor {

	@Override
	public <T extends Queueable> IConsumerProcess<T> makeProcess(T bean,
			IPublisher<T> publisher, boolean blocking) {
		return new MonitorAtomProcess<T>(bean, publisher, blocking);
	}

	class MonitorAtomProcess <T extends Queueable> extends AbstractQueueProcessor<T> {

		public  MonitorAtomProcess(T bean, IPublisher<T> publisher, boolean blocking) {
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
