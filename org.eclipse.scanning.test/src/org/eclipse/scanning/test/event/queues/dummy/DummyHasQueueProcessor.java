package org.eclipse.scanning.test.event.queues.dummy;

import org.eclipse.scanning.api.event.EventException;

public class DummyHasQueueProcessor extends DummyProcessor<DummyHasQueue> {

	public DummyHasQueueProcessor() {
		super();
	}

	@Override
	public Class<DummyHasQueue> getBeanClass() {
		return DummyHasQueue.class;
	}

	@Override
	public void recoverBeanData(DummyHasQueue bean) throws EventException {
		beanName = bean.getName();
		beanPercentComplete = bean.getPercentComplete();
		execLatch = bean.getLatch();
	}

}
