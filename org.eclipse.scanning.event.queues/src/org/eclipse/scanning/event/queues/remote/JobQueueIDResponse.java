package org.eclipse.scanning.event.queues.remote;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.IQueueControllerService;
import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.remote.QueueRequest;
import org.eclipse.scanning.event.queues.ServicesHolder;

/**
 * A response which returns the the ID of the job-queue configured in the 
 * {@link IQueueService} that the parent responder is associated with. 
 * 
 * @author Michael Wharmby
 *
 */
public class JobQueueIDResponse implements IQueueReponseStrategy {
	
	private IQueueControllerService queueControl;
	
	public JobQueueIDResponse() {
		queueControl = ServicesHolder.getQueueControllerService();
	}

	@Override
	public QueueRequest doResponse(QueueRequest request) throws EventException {
		String jobQueueID = queueControl.getJobQueueID();
		request.setJobQueueID(jobQueueID);
		return request;
	}

}
