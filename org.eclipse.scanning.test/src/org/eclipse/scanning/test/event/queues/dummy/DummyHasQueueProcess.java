package org.eclipse.scanning.test.event.queues.dummy;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.beans.Queueable;

public class DummyHasQueueProcess<T extends Queueable> extends DummyProcess<DummyHasQueue, T> {
	
	public static final String BEAN_CLASS_NAME = DummyHasQueue.class.getName();

	public DummyHasQueueProcess(T bean, IPublisher<T> publisher, Boolean blocking) throws EventException {
		super(bean, publisher, blocking);
	}

	@Override
	public Class<DummyHasQueue> getBeanClass() {
		return DummyHasQueue.class;
	}

}
