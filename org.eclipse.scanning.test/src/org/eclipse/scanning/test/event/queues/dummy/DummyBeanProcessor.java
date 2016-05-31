package org.eclipse.scanning.test.event.queues.dummy;

import org.eclipse.scanning.api.event.EventException;

public class DummyBeanProcessor extends DummyProcessor<DummyBean> {

	public DummyBeanProcessor() {
		super();
	}

	@Override
	public Class<DummyBean> getBeanClass() {
		return DummyBean.class;
	}

	@Override
	public void recoverBeanData(DummyBean bean) throws EventException {
		beanName = bean.getName();
		beanPercentComplete = bean.getPercentComplete();
		execLatch = bean.getLatch();
	}

}
