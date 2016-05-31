package org.eclipse.scanning.test.event.queues.dummy;

import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.event.queues.QueueProcess;

public class DummyAtomProcessor <T extends Queueable> extends DummyProcessor<T, DummyAtom> {

	protected DummyAtomProcessor(QueueProcess<T> queueProc) {
		super(queueProc);
	}

	@Override
	public Class<DummyAtom> getBeanClass() {
		return DummyAtom.class;
	}

}
