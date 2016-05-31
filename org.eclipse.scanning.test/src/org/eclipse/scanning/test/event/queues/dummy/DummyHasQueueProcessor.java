package org.eclipse.scanning.test.event.queues.dummy;

import java.util.concurrent.CountDownLatch;

public class DummyHasQueueProcessor extends DummyProcessor<DummyHasQueue> {

	public DummyHasQueueProcessor() {
		super();
	}

	@Override
	public Class<DummyHasQueue> getBeanClass() {
		return DummyHasQueue.class;
	}

	@Override
	protected CountDownLatch getLatch() {
		return dummy.getLatch();
	}

}
