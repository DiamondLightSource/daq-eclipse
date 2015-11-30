package org.eclipse.scanning.sequencer;

import org.eclipse.scanning.api.sequence.ISequencer;
import org.eclipse.scanning.api.sequence.ISequencerService;
import org.eclipse.scanning.api.sequence.SequenceException;

public class SequencerServiceImpl implements ISequencerService {

	@Override
	public ISequencer createSequencer(String name) throws SequenceException {
		SequencerImpl impl = new SequencerImpl();
		impl.setName(name);
		return impl;
	}

}
