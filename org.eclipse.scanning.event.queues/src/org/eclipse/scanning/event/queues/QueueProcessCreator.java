package org.eclipse.scanning.event.queues;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.IQueueProcess;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private static final Logger logger = LoggerFactory.getLogger(QueueProcessCreator.class);

	private boolean blocking;
	private IQueueProcess<T> queueProcess;
	private IQueueProcessor<? extends Queueable> processor;

	public QueueProcessCreator(boolean blocking) { //TODO Do we need this?
		this.blocking = blocking;
	}

	@Override
	public IConsumerProcess<T> createProcess(T atomBean,
			IPublisher<T> statusNotifier) throws EventException {
		//Create the process
		queueProcess = new QueueProcess<>(atomBean, statusNotifier, blocking);
		
		//Create & configure the processor
		try {
			processor = QueueProcessorFactory.getProcessor(atomBean.getClass().getName());
		} catch (EventException evEx) {
			logger.error("Cannot create process for bean '"+atomBean.getName()+"': No processor registered for the bean class '"+atomBean.getClass().getName()+"'");
			throw evEx;
		}
		try {
			processor.setProcessBean(atomBean);
		} catch (EventException evEx) {
			logger.error("Cannot create process for bean '"+atomBean.getName()+"': given bean class does not match processor bean class");
			throw evEx;
		}
		processor.setQueueBroadcaster(queueProcess);
		
		//Finish process configuration & return
		queueProcess.setProcessor(processor);
		return queueProcess;
	}

}
