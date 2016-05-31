package org.eclipse.scanning.test.event.queues.dummy;

import org.eclipse.scanning.api.event.queues.beans.QueueAtom;

/**
 * Class to mock behaviour of a generic QueueAtom. This can be processed 
 * within an active-queue.
 * 
 * @author Michael Wharmby
 *
 */
public class DummyAtom extends QueueAtom {
	
	public DummyAtom() {
		super();
	}
	
	public DummyAtom(String name, long time) {
		super();
		setName(name);
		runTime = time;
	}

}
