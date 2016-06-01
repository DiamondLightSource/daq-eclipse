package org.eclipse.scanning.test.event.queues.dummy;

public class DummyAtomProcessor extends DummyProcessor<DummyAtom> {
	
	public DummyAtomProcessor() {
		super();
	}

	@Override
	public Class<DummyAtom> getBeanClass() {
		return DummyAtom.class;
	}

}
