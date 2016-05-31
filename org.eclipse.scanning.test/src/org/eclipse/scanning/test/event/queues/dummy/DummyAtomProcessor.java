package org.eclipse.scanning.test.event.queues.dummy;

import java.util.concurrent.CountDownLatch;

public class DummyAtomProcessor extends DummyProcessor<DummyAtom> {
	
	public DummyAtomProcessor() {
		super();
	}

	@Override
	public Class<DummyAtom> getBeanClass() {
		return DummyAtom.class;
	}

	@Override
	protected CountDownLatch getLatch() {
		return dummy.getLatch();
	}

}
