package org.eclipse.scanning.event.queues.beans;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.beans.IHasAtomQueue;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.queues.beans.QueueBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TaskBean is a type of {@link QueueBean} implementing an 
 * {@link IHasAtomQueue}. As a {@link QueueBean} it can only be passed 
 * into the job-queue of an {@link IQueueService} and as such provides the most
 * abstract description of an experiment. 
 * 
 * This class of bean is used to describe a complete experiment, including the
 * beamline setup, data collection and post-processing steps. It should also 
 * contain all the sample metadata necessary to write the NeXus file. 
 * 
 * TODO Sample metadata holder.
 * FIXME java-doc
 * TODO This ought to be defined in dawnsci.analysis.api, to avoid dependencies in
 * 		this package
 * 
 * @author Michael Wharmby
 *
 */
public class TaskBean extends QueueBean implements IHasAtomQueue<SubTaskAtom> {

	/**
	 * Version ID for serialization. Should be updated when class changed. 
	 */
	private static final long serialVersionUID = 20161017L;

	private static final Logger logger = LoggerFactory.getLogger(TaskBean.class);
	
	private LinkedList<SubTaskAtom> atomQueue;
	private String queueMessage;
//	private Object nexusMetadata; TODO!!!!
	
	/**
	 * No argument constructor for JSON
	 */
	public TaskBean() {
		super();
		atomQueue = new LinkedList<>();
	}
	
	/**
	 * Basic constructor to set String name of bean
	 * @param name String user-supplied name
	 */
	public TaskBean(String name) {
		super();
		atomQueue = new LinkedList<>();
		setName(name);
	}

	@Override
	public List<SubTaskAtom> getAtomQueue() {
		return atomQueue;
	}

	@Override
	public void setAtomQueue(List<SubTaskAtom> atomQueue) {
		this.atomQueue = new LinkedList<>(atomQueue);
		setRunTime(calculateRunTime());
	}

	@Override
	public long calculateRunTime() {
		long runTime = 0;
		for (QueueAtom atom: atomQueue) {
			runTime = runTime + atom.getRunTime();
		}
		return runTime;
	}

	@Override
	public boolean addAtom(SubTaskAtom atom) {
		//Check that we're adding a real, non-duplicate atom to the queue
		if(atom == null) {
			logger.error("Attempting to add 'null' to queue.");
			throw new NullPointerException("Attempting to add null atom to AtomQueue");
		}
		if(isAtomPresent(atom)) {
			logger.error("Identical bean " + atom.getName()
					+ " (Class: "+ atom.getClass().getSimpleName()
					+ ") already in queue.");
			throw new IllegalArgumentException("Bean with identical UID already in queue.");
		}
		//Add atom, recalculate the runtime and return
		boolean result =  atomQueue.add(atom);
		setRunTime(calculateRunTime());
		return result;
	}

	@Override
	public int atomQueueSize() {
		return atomQueue.size();
	}

	@Override
	public int getIndex(String uid) {
		for (SubTaskAtom atom: atomQueue) {
			if (uid.equals(atom.getUniqueId())) return atomQueue.indexOf(atom);
		}
		throw new IllegalArgumentException("No queue element present with given UID");
	}

	@Override
	public SubTaskAtom nextAtom() {
		//Returns & removes first element of queue or throws NoSuchElementException if null
		return atomQueue.removeFirst();
	}

	@Override
	public SubTaskAtom viewNextAtom() {
		//Returns head of queue or throws NoSuchElementException if null.
		return atomQueue.getFirst();
	}

	@Override
	public boolean isAtomPresent(SubTaskAtom atom) {
		for (SubTaskAtom at: atomQueue) {
			String atomUID = atom.getUniqueId();
			if (atomUID.equals(at.getUniqueId())) return true;
		}
		return false;
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