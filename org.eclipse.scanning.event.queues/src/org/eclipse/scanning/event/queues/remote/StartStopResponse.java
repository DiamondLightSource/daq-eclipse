package org.eclipse.scanning.event.queues.remote;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.IQueueControllerService;
import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.remote.QueueRequest;
import org.eclipse.scanning.event.queues.ServicesHolder;

/**
 * A response which will start, stop or restart the {@link IQueueService} 
 * associated with the {@link IQueueControllerService}. The response returns 
 * the request to the user unchanged since there is no output from the 
 * start/stop calls, except an {@link EventException}.  
 * 
 * @author Michael Wharmby
 *
 */
public class StartStopResponse extends AbstractQueueResponseProcess {
	
	private IQueueControllerService queueControl;
	
	public StartStopResponse(QueueRequest requestBean, IPublisher<QueueRequest> reponseBroadcaster) {
		super(requestBean, reponseBroadcaster);
		queueControl = ServicesHolder.getQueueControllerService();
	}

	@Override
	public QueueRequest process(QueueRequest request) throws EventException {
		
		if (request.isStartQueueService() && request.isStopQueueService()) {
			//This is a restart
			queueControl.stopQueueService(request.isForceStop());
			queueControl.startQueueService();
		} else if (request.isStartQueueService()) {
			//Starting service
			queueControl.startQueueService();
		} else if (request.isStopQueueService()) {
			//Stopping service
			queueControl.stopQueueService(request.isForceStop());
		} else {
			throw new EventException("Request does not specify start or stop action");
		}
		
		return request;
	}
	


}
