package org.eclipse.scanning.event.queues.remote;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.IQueue;
import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.queues.remote.QueueRequest;
import org.eclipse.scanning.event.queues.ServicesHolder;
import org.eclipse.scanning.event.remote._Queue;

/**
 * A response which returns a remote {@link IQueue} ({@link _Queue}) object 
 * containing all of the configuration options of a real queue from the 
 * {@link IQueueService}. The queue which will have its configuration returned 
 * is determined from the queueID field of the request.
 *   
 * @author Michael Wharmby
 *
 */
public class GetQueueResponse extends AbstractQueueResponseProcess {
	
	private IQueueService queueService;

	protected GetQueueResponse(QueueRequest requestBean, IPublisher<QueueRequest> reponseBroadcaster) {
		super(requestBean, reponseBroadcaster);
		queueService = ServicesHolder.getQueueService();
	}

	@Override
	public QueueRequest process(QueueRequest request) throws EventException {
		//Get the requested queue & make a local copy with it
		IQueue<? extends Queueable> realQueue = queueService.getQueue(request.getQueueID());
		IQueue<? extends Queueable> remoteQueue = new _Queue<>(realQueue);
		
		//Send the remote copy back to the requester
		request.setQueue(remoteQueue);
		return request;
	}

}
