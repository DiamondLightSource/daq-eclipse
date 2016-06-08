package org.eclipse.scanning.test.event.queues.dummy;

import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.test.event.queues.processors.ScanAtomProcessorTest;

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
	
	public void merge(DummyHasQueue with) {
		super.merge(with);
		this.queueMessage = with.queueMessage;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((queueMessage == null) ? 0 : queueMessage.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DummyHasQueue other = (DummyHasQueue) obj;
		if (queueMessage == null) {
			if (other.queueMessage != null)
				return false;
		} else if (!queueMessage.equals(other.queueMessage))
			return false;
		return true;
	}
	
	

}
