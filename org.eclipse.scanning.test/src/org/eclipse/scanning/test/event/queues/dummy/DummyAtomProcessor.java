package org.eclipse.scanning.test.event.queues.dummy;

import org.eclipse.scanning.api.event.EventException;

public class DummyAtomProcessor extends DummyProcessor<DummyAtom> {

	public DummyAtomProcessor() {
		super();
	}

	@Override
	public Class<DummyAtom> getBeanClass() {
		return DummyAtom.class;
	}

	@Override
	public void recoverBeanData(DummyAtom bean) throws EventException {
		beanName = bean.getName();
		beanPercentComplete = bean.getPercentComplete();
		execLatch = bean.getLatch();
	}

}
