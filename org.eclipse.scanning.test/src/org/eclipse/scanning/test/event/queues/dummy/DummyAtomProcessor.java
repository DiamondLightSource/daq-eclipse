package org.eclipse.scanning.test.event.queues.dummy;

public class DummyAtomProcessor extends DummyProcessor<DummyAtom> {
	
	public DummyAtomProcessor(DummyAtom dummy) {
		super(dummy, dummy.getLatch());
	}

	@Override
	public Class<DummyAtom> getBeanClass() {
		return DummyAtom.class;
	}

}
