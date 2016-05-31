package org.eclipse.scanning.test.event.queues.dummy;

import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.event.queues.QueueProcess;

public class DummyHasQueueProcessor <T extends Queueable> extends DummyProcessor<T, DummyHasQueue> {

	protected DummyHasQueueProcessor(QueueProcess<T> queueProc) {
		super(queueProc);
	}

	@Override
	public Class<DummyHasQueue> getBeanClass() {
		return DummyHasQueue.class;
	}

}
