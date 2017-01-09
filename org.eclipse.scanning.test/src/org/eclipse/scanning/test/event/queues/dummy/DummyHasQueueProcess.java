package org.eclipse.scanning.test.event.queues.dummy;

public class DummyHasQueueProcessor extends DummyProcessor<DummyHasQueue> {

	public DummyHasQueueProcessor() {
		super();
	}

	@Override
	public Class<DummyHasQueue> getBeanClass() {
		return DummyHasQueue.class;
	}

}
