package org.eclipse.scanning.event.queues.processors;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.event.queues.beans.MoveAtom;

public class MoveAtomProcessor extends AbstractQueueProcessor<MoveAtom> implements IQueueProcessor<MoveAtom> {

	@Override
	public void execute() throws EventException, InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() throws EventException {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() throws EventException {
		// TODO Auto-generated method stub

	}

	@Override
	public void terminate() throws EventException {
		// TODO Auto-generated method stub

	}

	@Override
	public Class<MoveAtom> getBeanClass() {
		// TODO Auto-generated method stub
		return null;
	}

}
