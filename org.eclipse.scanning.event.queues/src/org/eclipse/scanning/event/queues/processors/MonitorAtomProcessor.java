package org.eclipse.scanning.event.queues.processors;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.beans.MonitorAtom;

/**
 * MonitorAtomProcessor reads back a single value from a monitor. It will use 
 * the view detector methods discussed that should be available as part of the
 * Mapping project. TODO!!!!
 * 
 * TODO Implement class!!!
 * TODO Rehash java-doc once implemented
 * TODO Add test of wrong bean type before cast.
 * 
 * @author Michael Wharmby
 *
 * @param <T> Bean implementing {@link Queueable}, but must be a 
 *            {@link MonitorAtom}.
 */
public class MonitorAtomProcessor extends AbstractQueueProcessor<MonitorAtom> {

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
	public Class<MonitorAtom> getBeanClass() {
		return MonitorAtom.class;
	}

}
