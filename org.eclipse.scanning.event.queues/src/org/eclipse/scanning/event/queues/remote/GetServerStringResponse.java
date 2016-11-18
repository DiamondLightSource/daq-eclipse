package org.eclipse.scanning.event.queues.remote;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
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
public class GetServerStringResponse extends AbstractQueueResponseProcess {
	
	private IQueueControllerService queueControl;
	
	public GetServerStringResponse(QueueRequest requestBean, IPublisher<QueueRequest> reponseBroadcaster) {
		super(requestBean, reponseBroadcaster);
		queueControl = ServicesHolder.getQueueControllerService();
	}

	@Override
	public QueueRequest process(QueueRequest request) throws EventException {
		switch (request.getRequestType()) {
		case COMMAND_SET:		request.setCommandSetName(queueControl.getCommandSetName());
								break;
		case COMMAND_TOPIC:		request.setCommandTopicName(queueControl.getCommandTopicName());
								break;
		case HEARTBEAT_TOPIC:	request.setHeartbeatTopicName(queueControl.getHeartbeatTopicName());
								break;
		case JOB_QUEUE_ID:		request.setJobQueueID(queueControl.getJobQueueID());
								break;
		default: throw new EventException("Unsupported QueueRequestType");
		}
		return request;
	}
}
