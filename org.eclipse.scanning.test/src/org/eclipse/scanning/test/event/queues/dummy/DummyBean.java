package org.eclipse.scanning.test.event.queues.dummy;

import org.eclipse.scanning.api.event.queues.beans.QueueBean;

/**
 * Class to mock behaviour of a generic QueueBean. This can be processed only
 * within a job-queue.
 * 
 * @author Michael Wharmby
 *
 */
public class DummyBean extends QueueBean {
	
	public DummyBean() {
		super();
	}
	
	public DummyBean(String name, long time) {
		super();
		setName(name);
		runTime = time;
	}

}
