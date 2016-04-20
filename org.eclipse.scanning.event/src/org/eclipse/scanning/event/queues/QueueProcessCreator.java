package org.eclipse.scanning.event.queues;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.event.queues.beans.MonitorAtom;
import org.eclipse.scanning.event.queues.beans.MoveAtom;
import org.eclipse.scanning.event.queues.beans.ScanAtom;
import org.eclipse.scanning.event.queues.beans.SubTaskBean;
import org.eclipse.scanning.event.queues.beans.TaskBean;
import org.eclipse.scanning.event.queues.processors.AtomQueueProcessor;
import org.eclipse.scanning.event.queues.processors.MonitorAtomProcessor;
import org.eclipse.scanning.event.queues.processors.MoveAtomProcessor;
import org.eclipse.scanning.event.queues.processors.ScanAtomProcessor;

/**
 * QueueProcessCreator creates the class which processes a given atom/bean. 
 * The processor returned depends on the type of the atom/bean.
 * 
 * @author Michael Wharmby
 *
 * @param <T> Base type of atom/bean operated on by the queue, e.g. 
 *            {@link QueueAtom} or {@QueueBean}.
 */
public class QueueProcessCreator<T extends Queueable> implements IProcessCreator<T> {

	private Map<String, IQueueProcessor> processMap;
	private boolean blocking;

	public QueueProcessCreator(boolean blocking) {
		this.blocking = blocking;
	}

	@Override
	public IConsumerProcess<T> createProcess(T atomBean,
			IPublisher<T> statusNotifier) throws EventException {

		//Create a map of bean type to processor
		processMap = new HashMap<String, IQueueProcessor>();
		processMap.put(MonitorAtom.class.getSimpleName(), new MonitorAtomProcessor());
		processMap.put(MoveAtom.class.getSimpleName(), new MoveAtomProcessor());
		processMap.put(ScanAtom.class.getSimpleName(), new ScanAtomProcessor());
		processMap.put(SubTaskBean.class.getSimpleName(), new AtomQueueProcessor());
		processMap.put(TaskBean.class.getSimpleName(), new AtomQueueProcessor());

		//Determine the type of bean and return the appropriate processor
		String className = atomBean.getClass().getSimpleName();
		if (processMap.containsKey(className)) {
			if(processMap.get(className) == null) throw new EventException("No processor registered for bean");
			return processMap.get(className).makeProcess(atomBean, statusNotifier, blocking);
		}
		throw new EventException("Bean type '"+className+"' not registered in queue processMap.");
	}

}
