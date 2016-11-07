package org.eclipse.scanning.event.queues.remote;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.remote.QueueRequest;

/**
 * A strategy interface for use as part of the {@link QueueResponseProcess} 
 * which allows requests for different types of information or data from the 
 * {@link IQueueService} to be processed in the same fashion.  
 * 
 * @author Michael Wharmby
 *
 */
public interface IQueueReponseStrategy {
	
	/**
	 * Makes necessary calls to the {@link IQueueService} to fulfil the request
	 * described by the {@link QueueRequest} argument. Fields on the given 
	 * {@link QueueRequest} are populated and the request is returned to the 
	 * {@link QueueResponseProcess}.
	 * 
	 * @param request {@link QueueRequest} defining work to be done.
	 * @return {@link QueueRequest} with requested fields populated. 
	 * @throws EventException in case of errors interacting with 
	 *         {@link IQueueService} or underlying {@link IEventService}.
	 */
	public QueueRequest doResponse(QueueRequest request) throws EventException;

}
