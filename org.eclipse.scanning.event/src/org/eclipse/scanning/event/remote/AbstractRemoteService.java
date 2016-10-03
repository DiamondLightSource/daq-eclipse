package org.eclipse.scanning.event.remote;

import java.io.Closeable;
import java.io.IOException;
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
abstract class AbstractRemoteService implements IDisconnectable, Closeable {

	protected IEventService eservice;
	protected URI uri;
	private boolean isDisconnected;
	
	protected AbstractRemoteService() {
		this.isDisconnected = false;
	}

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
	
	public void close() throws IOException {
		try {
			disconnect();
		} catch (EventException e) {
			throw new IOException(e);
		}
	}

	public boolean isDisconnected() {
		return isDisconnected;
	}

	public void setDisconnected(boolean isDisconnected) {
		this.isDisconnected = isDisconnected;
	}
}
