package org.eclipse.scanning.test.event.queues.dummy;

import java.util.concurrent.CountDownLatch;

public class DummyBeanProcessor extends DummyProcessor<DummyBean> {

	public DummyBeanProcessor() {
		super();
	}

	@Override
	public Class<DummyBean> getBeanClass() {
		return DummyBean.class;
	}

	@Override
	protected CountDownLatch getLatch() {
		return dummy.getLatch();
	}

}
