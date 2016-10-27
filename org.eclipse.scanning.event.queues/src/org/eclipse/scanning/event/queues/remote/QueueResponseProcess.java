package org.eclipse.scanning.event.queues.remote;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IResponseProcess;
import org.eclipse.scanning.api.event.queues.IQueueControllerService;
import org.eclipse.scanning.api.event.queues.remote.QueueRequest;
import org.eclipse.scanning.api.event.queues.remote.QueueRequestType;
import org.eclipse.scanning.event.queues.ServicesHolder;

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
	
	private final IQueueControllerService queueControl;
	
	public QueueResponseProcess(QueueRequest requestBean, IPublisher<QueueRequest> reponseBroadcaster) {
		this.requestBean = requestBean;
		this.reponseBroadcaster = reponseBroadcaster;
		
		queueControl = ServicesHolder.getQueueControllerService();
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
		switch (requestBean.getRequestType()) {
		case BEAN_STATUS:	return getBeanStatus();
		case JOB_QUEUE_ID:	return getJobQueueID();
		default: throw new EventException("Unknown QueueRequestType");
		}
	}
	
	private QueueRequest getBeanStatus() {
		return null;
		
	}
	
	/**
	 * Gets the job-queue ID of the {@link IQueueService} through the 
	 * {@link IQueueControllerService}.
	 * 
	 * @return {@link QueueRequest} with jobQueueID field populated.
	 */
	private QueueRequest getJobQueueID() {
		String jobQueueID = queueControl.getJobQueueID();
		requestBean.setJobQueueID(jobQueueID);
		return requestBean;
	}

}
