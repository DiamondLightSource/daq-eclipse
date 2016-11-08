package org.eclipse.scanning.event.queues.remote;

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
 * value, a class extending {@link QueueResponseProcess} is selected (through 
 * a strategy pattern) to populate the necessary fields in the request. 
 * Finally the {@link QueueRequest} is passed back to the parent 
 * {@link IResponder}. 
 * 
 * @author Michael Wharmby
 *
 */
public abstract class QueueResponseProcess implements IResponseProcess<QueueRequest> {
	
	private final QueueRequest requestBean;
	private final IPublisher<QueueRequest> reponseBroadcaster;
	
	protected QueueResponseProcess(QueueRequest requestBean, IPublisher<QueueRequest> reponseBroadcaster) {
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
	
}
