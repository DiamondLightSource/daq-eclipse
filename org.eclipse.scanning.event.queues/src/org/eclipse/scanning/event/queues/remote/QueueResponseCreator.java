package org.eclipse.scanning.event.queues.remote;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IResponseCreator;
import org.eclipse.scanning.api.event.core.IResponseProcess;
import org.eclipse.scanning.api.event.queues.remote.QueueRequest;

/**
 * Class responsible for creating the {@link AbstractQueueResponseProcess} objects.
 * 
 * @author Michael Wharmby
 *
 */
public class QueueResponseCreator implements IResponseCreator<QueueRequest> {

	@Override
	public IResponseProcess<QueueRequest> createResponder(QueueRequest requestBean, IPublisher<QueueRequest> reponseBroadcaster)
			throws EventException {
		//Decide which strategy to use to interrogate the QueueService
		switch (requestBean.getRequestType()) {
		case BEAN_STATUS:			
			return new BeanStatusReponse(requestBean, reponseBroadcaster);

		case COMMAND_SET:	// Options COMMAND_SET to JOB_QUEUE_ID all use the GetServerStringResponse
		case COMMAND_TOPIC:		
		case HEARTBEAT_TOPIC:	
		case JOB_QUEUE_ID:			
			return new GetServerStringResponse(requestBean, reponseBroadcaster);

		case SERVICE_START_STOP:	
			return new StartStopResponse(requestBean, reponseBroadcaster);

		default: throw new EventException("Unknown queue response request");
		}
	}

}
