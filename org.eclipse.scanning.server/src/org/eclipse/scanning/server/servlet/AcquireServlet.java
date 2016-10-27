package org.eclipse.scanning.server.servlet;

import static org.eclipse.scanning.api.event.EventConstants.ACQUIRE_REQUEST_TOPIC;
import static org.eclipse.scanning.api.event.EventConstants.ACQUIRE_RESPONSE_TOPIC;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IResponseProcess;
import org.eclipse.scanning.api.event.scan.AcquireRequest;

/**
 * A servlet to acquire data from a particular detector.
 */
public class AcquireServlet extends AbstractResponderServlet<AcquireRequest> {
	
	public AcquireServlet() {
		super(ACQUIRE_REQUEST_TOPIC, ACQUIRE_RESPONSE_TOPIC);
	}

	@Override
	public IResponseProcess<AcquireRequest> createResponder(AcquireRequest bean,
			IPublisher<AcquireRequest> response) throws EventException {
		return new AcquireRequestHandler(bean, response);
	}

}
