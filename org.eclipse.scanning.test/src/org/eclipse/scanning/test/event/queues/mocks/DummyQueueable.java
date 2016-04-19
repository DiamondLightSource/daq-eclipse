package org.eclipse.scanning.test.event.queues.mocks;

import org.eclipse.scanning.api.event.queues.beans.Queueable;

/**
 * Generic class to mock behaviour of a POJO in a Queue. Has an additional 
 * queue message option for testing a second message field (e.g. see 
 * {@link ScanAtomProcessorTest}).
 * 
 * @author Michael Wharmby
 *
 */
public class DummyQueueable extends Queueable {
	
	private String queueMessage;
	
	public DummyQueueable() {
		super();
	}
	
	public DummyQueueable(String name, long time) {
		super();
		setName(name);
		runTime = time;
	}

	public String getQueueMessage() {
		return queueMessage;
	}

	public void setQueueMessage(String queueMessage) {
		this.queueMessage = queueMessage;
	}

}
