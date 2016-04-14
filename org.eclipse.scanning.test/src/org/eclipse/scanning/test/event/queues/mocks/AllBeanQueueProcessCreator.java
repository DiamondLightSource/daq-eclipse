package org.eclipse.scanning.test.event.queues.mocks;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.beans.Queueable;

public class AllBeanQueueProcessCreator<T extends Queueable> implements IProcessCreator<T> {
	
	private Map<String, IConsumerProcess<T>> processMap;
	private boolean blocking;
	
	public AllBeanQueueProcessCreator(boolean blocking) {
		this.blocking = blocking;
	}

	@Override
	public IConsumerProcess<T> createProcess(T atomBean,
			IPublisher<T> statusNotifier) throws EventException {
		//Create a map of bean type to processor
		processMap = new HashMap<String, IConsumerProcess<T>>();
		processMap.put(DummyAtom.class.getSimpleName(), new DummyProcessor<T>(atomBean, statusNotifier, blocking));
		processMap.put(DummyBean.class.getSimpleName(), new DummyProcessor<T>(atomBean, statusNotifier, blocking));
		//FIXME Waiting for move.
//		processMap.put(MonitorAtom.class.getSimpleName(), new MonitorAtomProcessor<T>(atomBean, statusNotifier, blocking));
//		processMap.put(MoveAtom.class.getSimpleName(), new MoveAtomProcessor<T>(atomBean, statusNotifier, blocking));
//		processMap.put(ScanAtom.class.getSimpleName(), new ScanAtomProcessor<T>(atomBean, statusNotifier, blocking));
//		processMap.put(SubTaskBean.class.getSimpleName(), new AtomQueueProcessor<T>(atomBean, statusNotifier, blocking));
//		processMap.put(TaskBean.class.getSimpleName(), new AtomQueueProcessor<T>(atomBean, statusNotifier, blocking));
		
		//Determine the type of bean and return the appropriate processor
		String className = atomBean.getClass().getSimpleName();
		if (processMap.containsKey(className)) {
			if(processMap.get(className) == null) throw new EventException("No processor registered for bean");
			return processMap.get(className);
		}
		throw new EventException("Bean type not registered in queue processMap.");
	}

}
