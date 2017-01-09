package org.eclipse.scanning.test.event.queues.dummy;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.beans.Queueable;

public class DummyHasQueueProcess<T extends Queueable> extends DummyProcess<DummyHasQueue, T> {

	public DummyHasQueueProcess(T bean, IPublisher<T> publisher) throws EventException {
		super(bean, publisher, false);
	}

	@Override
	public Class<DummyHasQueue> getBeanClass() {
		return DummyHasQueue.class;
	}

}
