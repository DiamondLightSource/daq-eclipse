package org.eclipse.scanning.event.queues.beans;

import org.eclipse.scanning.api.event.queues.beans.IAtomBeanWithQueue;
import org.eclipse.scanning.api.event.queues.beans.IAtomQueue;
import org.eclipse.scanning.api.event.queues.beans.QueueBean;

/**
 * TaskBean is a type of {@link QueueBean} implementing an 
 * {@link IAtomBeanWithQueue}. As a {@link QueueBean} it can only be passed 
 * into the job-queue of an {@link IQueueService} and as such provides the most
 * abstract description of an experiment. 
 * 
 * This class of bean is used to describe a complete experiment, including the
 * beamline setup, data collection and post-processing steps. It should also 
 * contain all the sample metadata necessary to write the NeXus file. 
 * 
 * TODO Sample metadata holder.
 * TODO This ought to be defined in dawnsci.analysis.api, to avoid dependencies in
 * 		this package
 * 
 * @author Michael Wharmby
 *
 */
public class TaskBean extends QueueBean implements IAtomBeanWithQueue<SubTaskAtom> {
	
	private IAtomQueue<SubTaskAtom> atomQueue = new AtomQueue<SubTaskAtom>();
	private String queueMessage;
	
	/**
	 * No argument constructor for JSON
	 */
	public TaskBean() {
		super();
	}
	
	/**
	 * Basic constructor to set String name of bean
	 * @param name String user-supplied name
	 */
	public TaskBean(String name) {
		super();
		setName(name);
	}

	@Override
	public IAtomQueue<SubTaskAtom> getAtomQueue() {
		return atomQueue;
	}

	@Override
	public void setAtomQueue(IAtomQueue<SubTaskAtom> atomQueue) {
		this.atomQueue = atomQueue;
	}
	
	/* (non-Javadoc)
	 * Ensures runTime value reported is that from the queue and not a 
	 * random value. 
	 * @see uk.ac.diamond.daq.queues.AbstractQueueBean#setRunTime(long)
	 */
	@Override
	public void setRunTime(long runTime) {
		this.runTime = runTime();
	}
	
	/* (non-Javadoc)
	 * Ensures runTime value reported is that from the queue and not a 
	 * random value. 
	 * @see uk.ac.diamond.daq.queues.AbstractQueueBean#getRunTime()
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
		TaskBean other = (TaskBean) obj;
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