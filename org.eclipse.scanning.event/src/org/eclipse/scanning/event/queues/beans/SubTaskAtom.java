package org.eclipse.scanning.event.queues.beans;

import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.beans.IAtomBeanWithQueue;
import org.eclipse.scanning.api.event.queues.beans.IAtomQueue;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;

/**
 * SubTaskBean is a type of {@link QueueAtom} implementing an 
 * {@link IAtomBeanWithQueue}. As a {@link QueueAtom} it can only be passed 
 * into an active-queue of an {@link IQueueService}.
 * 
 * This class of bean is used to describe a part of an experiment, for example 
 * motor moves and an I0 measurement before a scan or a series of motor moves, 
 * a scan and post-processing. It should be able to provide a pointer to the 
 * parent queue's sample metadata.
 * 
 * SubTaskBeans may be nested inside of SubTaskBeans to provide a hierarchy.
 * 
 * TODO Sample metadata?
 * TODO This ought to be defined in dawnsci.analysis.api, to avoid dependencies in
 * 		this package
 * 
 * @author Michael Wharmby
 * 
 */
public class SubTaskAtom extends QueueAtom implements IAtomBeanWithQueue<QueueAtom> {
	
	private IAtomQueue<QueueAtom> atomQueue = new AtomQueue<QueueAtom>();
	private String queueMessage;
	
	/**
	 * No argument constructor for JSON
	 */
	public SubTaskAtom() {
		super();
	}
	
	/**
	 * Basic constructor to set String name of bean
	 * @param name String user-supplied name
	 */
	public SubTaskAtom(String name) {
		super();
		setName(name);
	}
	
	@Override
	public IAtomQueue<QueueAtom> getAtomQueue() {
		return atomQueue;
	}

	@Override
	public void setAtomQueue(IAtomQueue<QueueAtom> atomQueue) {
		this.atomQueue = atomQueue;
	}
	

	/* (non-Javadoc)
	 * Ensures runTime value reported is that from the queue and not a 
	 * random value. 
	 * @see uk.ac.diamond.daq.queues.AbstractQueueAtom#setRunTime(long)
	 */
	@Override
	public void setRunTime(long runTime) {
		this.runTime = runTime();
	}
	
	/* (non-Javadoc)
	 * Ensures runTime value reported is that from the queue and not a 
	 * random value. 
	 * @see uk.ac.diamond.daq.queues.AbstractQueueAtom#getRunTime()
	 */
	@Override
	public long getRunTime() {
		return runTime();
	}

	@Override
	public String getQueueMessage() {
		return queueMessage;
	}

	@Override
	public void setQueueMessage(String msg) {
		queueMessage = msg;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((atomQueue == null) ? 0 : atomQueue.hashCode());
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
		SubTaskAtom other = (SubTaskAtom) obj;
		if (atomQueue == null) {
			if (other.atomQueue != null)
				return false;
		} else if (!atomQueue.equals(other.atomQueue))
			return false;
		if (queueMessage == null) {
			if (other.queueMessage != null)
				return false;
		} else if (!queueMessage.equals(other.queueMessage))
			return false;
		return true;
	}

}
