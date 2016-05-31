package org.eclipse.scanning.test.event.queues.dummy;

import java.util.concurrent.CountDownLatch;

import org.eclipse.scanning.api.event.queues.beans.Queueable;

/**
 * Generic class to mock behaviour of a POJO in a Queue. Has an additional 
 * queue message option for testing a second message field (e.g. see 
 * {@link ScanAtomProcessorTest}).
 * 
 * @author Michael Wharmby
 *
 */
public class DummyHasQueue extends Queueable {
	
	private String queueMessage;
	private CountDownLatch latch;
	
	public DummyHasQueue() {
		super();
	}
	
	public DummyHasQueue(String name, long time) {
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

	public CountDownLatch getLatch() {
		return latch;
	}

	public void setLatch(CountDownLatch latch) {
		this.latch = latch;
	}

}
