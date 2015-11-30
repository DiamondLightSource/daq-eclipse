package org.eclipse.scanning.test.sequencer;

import org.eclipse.scanning.api.sequence.ISequencer;
import org.eclipse.scanning.api.sequence.ISequencerService;
import org.eclipse.scanning.api.sequence.SequenceException;
import org.eclipse.scanning.sequencer.SequencerServiceImpl;
import org.junit.Before;

public class SequencerTest {

	
	private ISequencerService service;
	private ISequencer        seq;
	
	@Before
	public void setup() throws SequenceException {
		service = new SequencerServiceImpl();
		seq = service.createSequencer("Test");
	}
}
