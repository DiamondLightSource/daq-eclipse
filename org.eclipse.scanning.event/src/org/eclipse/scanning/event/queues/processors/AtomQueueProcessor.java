package org.eclipse.scanning.event.queues.processors;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.queues.IQueueControllerService;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.beans.IHasAtomQueue;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.ServicesHolder;

/**
 * Generic class for processing a {@link Queueable} composed of an 
 * {@link IOLDAtomQueue}. The processor spools the atoms in the contained queue 
 * into a new queue created through the {@link IQueueService}. The new queue is
 * monitored using the {@link QueueListener} and through the queue service.
 * 
 * TODO Rehash java-doc once implemented
 * TODO Add test of wrong bean type before cast.
 * 
 * @author Michael Wharmby
 *
 * @param <P> Bean implementing {@link Queueable}, but must be an  
 *            {@link IHasAtomQueue}.
 * @param <Q> Bean from within the AtomQueue - implements {@link QueueAtom}.
 */
public class AtomQueueProcessor<P extends Queueable & IHasAtomQueue<Q>, Q extends QueueAtom> {
	
	private IQueueService queueService;
	private IQueueControllerService queueController;
	private QueueListener<P, Q> queueListener;
	private ISubscriber<QueueListener<P, Q>> queueSubscriber;
	
	private IQueueProcessor<P> parentProcessor;
	private String activeQueueName; 
	
	public AtomQueueProcessor(IQueueProcessor<P> parentProcessor) {
		queueService = ServicesHolder.getQueueService();
		queueController = ServicesHolder.getQueueControllerService();
		this.parentProcessor = parentProcessor;
	}
		
	public void run() throws EventException, InterruptedException {
		//Everything should be set up by now, so we can get the atomQueue
		final P atomQueue = parentProcessor.getProcessBean();
		
		//Create a new active queue to submit the atoms into
		parentProcessor.getQueueBroadcaster().broadcast(Status.RUNNING, 0d, "Registering new active queue.");
		activeQueueName = queueService.registerNewActiveQueue();
		
		//Spool beans from bean atom queue to the queue service
		//(queue empty after this!)
		parentProcessor.getQueueBroadcaster().broadcast(Status.RUNNING, 1d, "Submitting atoms to active queue.");
		Queueable parentBean = parentProcessor.getProcessBean();
		while (atomQueue.atomQueueSize() > 0) {
			QueueAtom nextAtom = atomQueue.viewNextAtom();
			if (nextAtom.getBeamline() != parentBean.getBeamline()) {
				nextAtom.setBeamline(parentBean.getBeamline());
			}
			if (nextAtom.getHostName() != parentBean.getHostName()) {
				nextAtom.setHostName(parentBean.getHostName());
			}
			if (nextAtom.getUserName() != parentBean.getUserName()) {
				nextAtom.setUserName(parentBean.getUserName());
			}
			queueController.submit(atomQueue.nextAtom(), activeQueueName);
		}
		
		//Create QueueListener
		queueListener = new QueueListener<>(
				parentProcessor.getQueueBroadcaster(), 
				parentProcessor.getProcessBean(), 
				parentProcessor.getProcessorLatch());
		queueSubscriber = queueController.createQueueSubscriber(activeQueueName);
		queueSubscriber.addListener(queueListener);
		
		//Start processing & wait for it to end.
		parentProcessor.getQueueBroadcaster().broadcast(Status.RUNNING, 4d, "Beans submitted. Starting active queue...");
		queueService.startActiveQueue(activeQueueName);
		parentProcessor.getQueueBroadcaster().broadcast(Status.RUNNING, 5d, "Waiting for active queue to complete");
		parentProcessor.getProcessorLatch().await();
	}
	
	public void tidyQueue() throws EventException {
		//Tidy up our processes, before handing back control
		queueService.stopActiveQueue(activeQueueName, false);
		queueService.deRegisterActiveQueue(activeQueueName, true);
		queueSubscriber.disconnect();
	}

	public String getActiveQueueName() {
		return activeQueueName;
	}
}
