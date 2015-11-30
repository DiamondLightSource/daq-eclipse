package org.eclipse.scanning.sequencer;

import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.sequence.ISequencer;
import org.eclipse.scanning.api.sequence.SequenceException;

class SequencerImpl implements ISequencer {
	
	private String name;

	@Override
	public Status getStatus() {
		return Status.NONE;
	}

	@Override
	public void execute() throws SequenceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void terminate() throws SequenceException {
		// TODO Auto-generated method stub
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
