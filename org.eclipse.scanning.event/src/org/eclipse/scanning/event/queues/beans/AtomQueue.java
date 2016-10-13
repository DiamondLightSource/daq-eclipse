package org.eclipse.scanning.event.queues.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.scanning.api.event.queues.beans.IAtomBeanWithQueue;
import org.eclipse.scanning.api.event.queues.beans.IAtomQueue;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AtomQueue is a concrete implementation of {@link IAtomQueue}, describing the
 * queue used to store {@link QueueAtoms} as they are transferred through the 
 * {@link IQueueService}. Examples of its use in action can be found in classes
 * implementing the {@link IAtomBeanWithQueue} interface (e.g. 
 * {@link SubTaskAtom}).
 * 
 * TODO This ought to be defined in dawnsci.analysis.api, to avoid dependencies in
 * 		this package
 * 
 * @author Michael Wharmby
 *
 * @param <T> Type extending {@link QueueAtom} forming the child queue produced
 *            by processing POJOs with this class.
 */
public class AtomQueue<T extends QueueAtom> implements IAtomQueue<T> {
	
	private static final Logger logger = LoggerFactory.getLogger(AtomQueue.class);
	
	private LinkedList<T> queue;
	private long runTime = 0;
	
	public AtomQueue() {
		queue = new LinkedList<T>();
	}

	@Override
	public List<T> getQueue() {
		return queue;
	}
	
	@Override
	public void setQueue(List<T> queue) {
		this.queue = new LinkedList<T>(queue);
	}

	@Override
	public long getRunTime() {
		return runTime;
	}
	
	@Override
	public void setRunTime(long runTime) {
		this.runTime = runTime;
	}

	@Override
	public long calculateRunTime() {
		long runTime = 0;
		for (QueueAtom atom: queue) {
			runTime = runTime + atom.getRunTime();
		}
		return runTime;
	}

	@Override
	public boolean add(T atom) {
		if(atom == null) throw new NullPointerException("Attempting to add null atom to AtomQueue");
		if(isAtomPresent(atom)) {
			logger.error("Identical bean " + atom.getName()
					+ " (Class: "+ atom.getClass().getSimpleName()
					+ ") already in queue.");
			return false;
		}
		boolean result =  queue.add(atom);
		runTime = calculateRunTime();
		return result;
	}

	@Override
	public boolean add(T atom, int index) {
		if(atom == null) throw new NullPointerException("Attempting to add null atom to AtomQueue");
		if(isAtomPresent(atom)) {
			logger.error("Identical bean " + atom.getName()
					+ " (Class: "+ atom.getClass().getSimpleName()
					+ ") already in queue.");
			return false;
		}
		queue.add(index, atom);
		boolean result =  queue.contains(atom);
		runTime = calculateRunTime();
		return result;
	}

	@Override
	public boolean addList(Collection<T> atomList) {
		if(atomList.contains(null) || atomList.isEmpty()) throw new NullPointerException("Attempting to add null or empty list to AtomQueue");
		if(isAtomInListPresent(atomList)) {
			logger.error("Identical bean in submitted collection or already in queue.");
			return false;
		}
		boolean result = queue.addAll(atomList);
		runTime = calculateRunTime();
		return result;
	}

	@Override
	public boolean addList(Collection<T> atomList, int index) {
		if(atomList.contains(null) || atomList.isEmpty()) throw new NullPointerException("Attempting to add null or empty list to AtomQueue");
		if(isAtomInListPresent(atomList)) {
			logger.error("Identical bean in submitted collection or already in queue.");
			return false;
		}
		boolean result = queue.addAll(index, atomList);
		runTime = calculateRunTime();
		return result;
	}

	@Override
	public boolean remove(int index) {
		QueueAtom at = queue.remove(index);
		runTime = calculateRunTime();
		return !queue.contains(at);
	}

	@Override
	public boolean remove(String uid) {
		QueueAtom at = queue.remove(getIndex(uid));
		runTime = calculateRunTime();
		return !queue.contains(at);
	}

	@Override
	public int queueSize() {
		return queue.size();
	}

	@Override
	public int getIndex(String uid) {
		for (QueueAtom atom: queue) {
			if (uid.equals(atom.getUniqueId())) return queue.indexOf(atom);
		}
		throw new IllegalArgumentException("No queue element present with given UID");
	}

	@Override
	public T view(int index) {
		return queue.get(index);
	}

	@Override
	public T view(String uid) {
		return queue.get(getIndex(uid));
	}

	@Override
	public T next() {
		return queue.removeFirst();
	}

	@Override
	public T viewNext() {
		return queue.getFirst();
	}

	@Override
	public T last() {
		return queue.removeLast();
	}

	@Override
	public T viewLast() {
		return queue.getLast();
	}

	@Override
	public ListIterator<T> getQueueIterator() {
		return queue.listIterator();
	}

	@Override
	public boolean isAtomPresent(T atom) {
		for (QueueAtom at: queue) {
			String atomUID = atom.getUniqueId();
			if (atomUID.equals(at.getUniqueId())) return true;
		}
		return false;
	}

	@Override
	public boolean isAtomInListPresent(Collection<T> atomSet) {
		List<T> tmpList = new ArrayList<>(atomSet);
		T at;
		String atId;
		
		//Compare every atom in the list against every other atom once
		for(int i = 0; i < tmpList.size(); i++) {
			at = tmpList.get(i);
			atId = at.getUniqueId();
			for(int j = i+1; j < tmpList.size(); j++) {
				if (atId.equals(tmpList.get(j).getUniqueId())) return true;
			}
		if (isAtomPresent(at)) return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((queue == null) ? 0 : queue.hashCode());
		result = prime * result + (int) (runTime ^ (runTime >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AtomQueue<?> other = (AtomQueue<?>) obj;
		if (queue == null) {
			if (other.queue != null)
				return false;
		} else if (!queue.equals(other.queue))
			return false;
		if (runTime != other.runTime)
			return false;
		return true;
	}

}
