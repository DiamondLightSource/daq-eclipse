package org.eclipse.scanning.event.queues.remote;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IResponder;
import org.eclipse.scanning.api.event.core.IResponseProcess;
import org.eclipse.scanning.api.event.queues.IQueueControllerService;
import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.remote.QueueRequest;
import org.eclipse.scanning.api.event.queues.remote.QueueRequestType;

/**
 * Process to provide remote access to configuration information and data 
 * stored in the {@link IQueueService}, with access through 
 * {@link IQueueControllerService}. Requests are received as 
 * {@link QueueRequest} beans, with a {@link QueueRequestType}. Based on this 
 * value, an {@link IQueueReponseStrategy} is selected to get the populated 
 * the necessary fields in the request. Finally the {@link QueueRequest} is 
 * passed back to the parent {@link IResponder}. 
 * 
 * @author Michael Wharmby
 *
 */
public class QueueResponseProcess implements IResponseProcess<QueueRequest> {
	
	private final QueueRequest requestBean;
	private final IPublisher<QueueRequest> reponseBroadcaster;
	private IQueueReponseStrategy responder;
	
	public QueueResponseProcess(QueueRequest requestBean, IPublisher<QueueRequest> reponseBroadcaster) {
		this.requestBean = requestBean;
		this.reponseBroadcaster = reponseBroadcaster;
	}

	@Override
	public QueueRequest getBean() {
		return requestBean;
	}

	@Override
	public IPublisher<QueueRequest> getPublisher() {
		return reponseBroadcaster;
	}

	@Override
	public QueueRequest process(QueueRequest request) throws EventException {
		
		
		//Decide which strategy to use to interrogate the QueueService
		switch (requestBean.getRequestType()) {
		case BEAN_STATUS:			
			responder = new BeanStatusReponse();
			break;
		
		case COMMAND_SET:	// Options COMMAND_SET to JOB_QUEUE_ID all use the GetServerStringResponse
		case COMMAND_TOPIC:		
		case HEARTBEAT_TOPIC:	
		case JOB_QUEUE_ID:			
			responder = new GetServerStringResponse();
			break;
		
		case SERVICE_START_STOP:	
			responder = new StartStopResponse();
			break;
		
		default: responder = null;
		}
		if (responder == null) throw new EventException("Unsupported QueueRequestType");
		return responder.doResponse(request);
	}


}
