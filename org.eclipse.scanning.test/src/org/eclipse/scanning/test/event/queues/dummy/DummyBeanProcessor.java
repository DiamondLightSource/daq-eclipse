package org.eclipse.scanning.test.event.queues.dummy;

import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.event.queues.QueueProcess;

public class DummyBeanProcessor <T extends Queueable> extends DummyProcessor<T, DummyBean> {

	protected DummyBeanProcessor(QueueProcess<T> queueProc) {
		super(queueProc);
	}

	@Override
	public Class<DummyBean> getBeanClass() {
		return DummyBean.class;
	}

}
