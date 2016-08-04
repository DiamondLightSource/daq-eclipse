package org.eclipse.scanning.event.queues.processors;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.beans.IAtomBeanWithQueue;
import org.eclipse.scanning.api.event.queues.beans.IAtomQueue;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.AtomQueueServiceUtils;
import org.eclipse.scanning.event.queues.QueueServicesHolder;
import org.eclipse.scanning.event.queues.beans.SubTaskBean;

/**
 * Generic class for processing a {@link Queueable} composed of an 
 * {@link IAtomQueue}. The processor spools the atoms in the contained queue 
 * into a new queue created through the {@link IQueueService}. The new queue is
 * monitored using the {@link QueueListener} and through the queue service.
 * 
 * TODO Implement class!!!
 * TODO Rehash java-doc once implemented
 * TODO Add test of wrong bean type before cast.
 * 
 * @author Michael Wharmby
 *
 * @param <T> Bean implementing {@link Queueable}, but must be an 
 *            {@link IAtomBeanWithQueue}.
 */
public class AtomQueueProcessor {
	
	private IQueueService queueService;
	private QueueListener<SubTaskBean, QueueAtom> queueListener;
	private ISubscriber<QueueListener<SubTaskBean, QueueAtom>> queueSubscriber;
	
	private SubTaskAtomProcessor parentProcessor;
	
	public AtomQueueProcessor(SubTaskAtomProcessor parentProcessor) {
		queueService = QueueServicesHolder.getQueueService();	
		this.parentProcessor = parentProcessor;
	}
		
	public void run() throws EventException, InterruptedException {
		//Everything should be set up by now, so we can get the atomQueue
		final IAtomQueue<QueueAtom> atomQueue = parentProcessor.getProcessBean().getAtomQueue();
		
		//Create a new active queue to submit the atoms into
		parentProcessor.getQueueBroadcaster().broadcast(Status.RUNNING, 0d, "Registering new active queue.");
		final String activeQueueName = queueService.registerNewActiveQueue();
		
		//Spool beans from bean atom queue to the queue service
		//(queue empty after this!)
		parentProcessor.getQueueBroadcaster().broadcast(Status.RUNNING, 1d, "Submitting atoms to active queue.");
		SubTaskBean parentBean = parentProcessor.getProcessBean();
		while (atomQueue.queueSize() > 0) {
			QueueAtom nextAtom = atomQueue.viewNext();
			if (nextAtom.getBeamline() != parentBean.getBeamline()) {
				nextAtom.setBeamline(parentBean.getBeamline());
			}
			if (nextAtom.getHostName() != parentBean.getHostName()) {
				nextAtom.setHostName(parentBean.getHostName());
			}
			if (nextAtom.getUserName() != parentBean.getUserName()) {
				nextAtom.setUserName(parentBean.getUserName());
			}
			queueService.submit(atomQueue.next(), activeQueueName);
		}
		
		//Create QueueListener
		queueListener = new QueueListener<>(
				parentProcessor.getQueueBroadcaster(), 
				parentProcessor.getProcessBean(), 
				parentProcessor.getProcessorLatch());
		queueSubscriber = AtomQueueServiceUtils.createActiveQueueSubscriber(activeQueueName);
		queueSubscriber.addListener(queueListener);
		
		//Start processing & wait for it to end.
		parentProcessor.getQueueBroadcaster().broadcast(Status.RUNNING, 4d, "Beans submitted. Starting active queue...");
		queueService.startActiveQueue(activeQueueName);
		parentProcessor.getQueueBroadcaster().broadcast(Status.RUNNING, 5d, "Waiting for active queue to complete");
		parentProcessor.getProcessorLatch().await();
		
		//Tidy up our processes, before handing back control
		queueService.stopActiveQueue(activeQueueName, false);
		queueService.deRegisterActiveQueue(activeQueueName, true);
		queueSubscriber.disconnect();
	}

}
