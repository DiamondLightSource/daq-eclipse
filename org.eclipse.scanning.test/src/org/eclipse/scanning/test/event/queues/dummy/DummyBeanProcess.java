package org.eclipse.scanning.test.event.queues.dummy;

public class DummyBeanProcessor extends DummyProcessor<DummyBean> {

	public DummyBeanProcessor() {
		super();
	}

	@Override
	public Class<DummyBean> getBeanClass() {
		return DummyBean.class;
	}

}
