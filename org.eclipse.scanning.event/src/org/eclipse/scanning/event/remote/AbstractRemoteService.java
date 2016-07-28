package org.eclipse.scanning.event.remote;

import java.net.URI;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IDisconnectable;

/**
 * 
 * @author Matthew Gerring
 *
 * @param <T> The service
 */
abstract class AbstractRemoteService implements IDisconnectable {

	protected IEventService eservice;
	protected URI uri;

	protected IEventService getEventService() {
		return eservice;
	}

	public void setEventService(IEventService eservice) {
		this.eservice = eservice;
	}

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	/**
	 * Does nothing, requires override.
	 */
	void init() throws EventException {
		
	}
}
