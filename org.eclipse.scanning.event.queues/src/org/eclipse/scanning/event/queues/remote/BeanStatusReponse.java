package org.eclipse.scanning.event.queues.remote;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.queues.remote.QueueRequest;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.ServicesHolder;

/**
 * A response which searches in both the submission queue and status set of an 
 * {@link IConsumer} for a bean with a given unique ID and then returns the
 * {@link Status} of the bean. The {@link IConsumer} to search is that 
 * associated with the {@link IQueue} with the queueID found on the request. 
 * 
 * @author Michael Wharmby
 *
 */
public class BeanStatusReponse implements IQueueReponseStrategy {
	
	private IQueueService queueService;
	private Status foundStatus = null;
	private CountDownLatch beanFoundLatch;
	
	public BeanStatusReponse() {
		queueService = ServicesHolder.getQueueService();
	}

	@Override
	public QueueRequest doResponse(QueueRequest request) throws EventException {
		//Get request details
		String beanID = request.getBeanID();
		String queueID = request.getQueueID();
		
		//Get the queue consumer requested
		IConsumer<? extends Queueable> consumer = queueService.getQueue(queueID).getConsumer();
		
		/*
		 * Interrogate both submission & status queues simultaneously.
		 */
		//This needs a latch to indicate complete...
		//final CountDownLatch 
		beanFoundLatch = new CountDownLatch(1);
		
		ExecutorService threadPool = Executors.newFixedThreadPool(2);
		Future<?> statusSetSearch = threadPool.submit(new QueueSearcher(beanID, consumer.getStatusSet()));//, beanFoundLatch));
		Future<?> submitQueueSearch = threadPool.submit(new QueueSearcher(beanID, consumer.getSubmissionQueue()));//, beanFoundLatch));
		
		boolean latched;
		try {
			latched = beanFoundLatch.await(5000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException iEx) {
			throw new EventException("Could not wait finding bean", iEx);
		} finally {
			//At this stage we can safely shutdown the executor.
			threadPool.shutdown();
		}
		if (!latched) {
			//Something went wrong, but was it an exception or just not found?
			try {
				statusSetSearch.get(1, TimeUnit.MILLISECONDS);
				submitQueueSearch.get(1, TimeUnit.MILLISECONDS);
				throw new EventException("Search timed out before finding bean. Contact your GDA representative.");
			} catch (Exception ex) {
				throw new EventException("Bean search encountered error(s)",ex);
			}
		}
		else if (foundStatus == null) throw new EventException("Bean not found");
		
		//We've got the status, put it into the reply & return
		request.setBeanStatus(foundStatus);
		return request;
	}
	
	/**
	 * Runnable which searches through a list of beans looking for one with 
	 * the given unique ID. Called by the threadPool in the outer class.
	 * 
	 * @author Michael Wharmby
	 *
	 */
	private class QueueSearcher implements Runnable {
		
		private final List<? extends Queueable> beanQueue;
		private final String beanID;
		
		private QueueSearcher(String beanID, List<? extends Queueable> beanQueue) {
			this.beanQueue = beanQueue;
			this.beanID = beanID;
		}

		@Override
		public void run() {
			for (Queueable queueItem : beanQueue) {
				if (queueItem.getUniqueId().equals(beanID)) {
					foundStatus = queueItem.getStatus();
					beanFoundLatch.countDown();
					break;
				}
			}
		}
		
	}

}
