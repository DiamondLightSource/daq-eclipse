package org.eclipse.scanning.event.queues.remote;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.IQueueControllerService;
import org.eclipse.scanning.api.event.queues.remote.QueueRequest;
import org.eclipse.scanning.event.queues.ServicesHolder;

public class StartStopResponse implements IQueueReponseStrategy {
	
	private IQueueControllerService queueControl;
	
	public StartStopResponse() {
		queueControl = ServicesHolder.getQueueControllerService();
	}

	@Override
	public QueueRequest doResponse(QueueRequest request) throws EventException {
		
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
