package org.eclipse.scanning.event.queues.remote;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IResponseCreator;
import org.eclipse.scanning.api.event.core.IResponseProcess;
import org.eclipse.scanning.api.event.queues.remote.QueueRequest;

/**
 * Class responsible for creating the {@link QueueResponseProcess} objects.
 * 
 * @author Michael Wharmby
 *
 */
public class QueueResponseCreator implements IResponseCreator<QueueRequest> {

	@Override
	public IResponseProcess<QueueRequest> createResponder(QueueRequest requestBean, IPublisher<QueueRequest> reponseBroadcaster)
			throws EventException {
		return new QueueResponseProcess(requestBean, reponseBroadcaster);
	}

}
