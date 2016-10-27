package org.eclipse.scanning.event.queues.remote;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IResponseProcess;
import org.eclipse.scanning.api.event.queues.remote.QueueRequest;
import org.eclipse.scanning.api.event.queues.remote.QueueRequestType;

/**
 * Process to provide remote access to data stored in the 
 * {@link IQueueService}, with access through {@link IQueueControllerService}.
 * Requests are received as {@link QueueRequest} beans, with a 
 * {@link QueueRequestType}. Based on this value, the {@link QueueRequest} has 
 * fields populated and is passed back to the parent {@link IResponder}. 
 * 
 * @author Michael Wharmby
 *
 */
public class QueueResponseProcess implements IResponseProcess<QueueRequest> {
	
	private final QueueRequest requestBean;
	private final IPublisher<QueueRequest> reponseBroadcaster;
	
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
		// TODO Auto-generated method stub
		return null;
	}

}
