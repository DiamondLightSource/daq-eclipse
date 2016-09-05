package org.eclipse.scanning.event.remote;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceRequest;

abstract class _AbstractRemoteDevice<M> extends AbstractRemoteService {

	
	protected String                          name;
	protected DeviceInformation<M>            info;
	protected final IRequester<DeviceRequest> requester;

	private _AbstractRemoteDevice(URI uri, IEventService eservice) throws EventException {
		setEventService(eservice);
		setUri(uri);
		requester = eservice.createRequestor(uri, EventConstants.DEVICE_REQUEST_TOPIC, EventConstants.DEVICE_RESPONSE_TOPIC);
	}

	@SuppressWarnings("unchecked")
	_AbstractRemoteDevice(DeviceRequest req, URI uri, IEventService eservice) throws EventException, InterruptedException {
		this(uri, eservice);
	    requester.setTimeout(RemoteServiceFactory.getTime(), RemoteServiceFactory.getTimeUnit()); // Useful for debugging testing 
	    connect(req);
	}

	_AbstractRemoteDevice(DeviceRequest req, long timeoutMs, URI uri, IEventService eservice) throws EventException, InterruptedException {
		this(uri, eservice);
	    requester.setTimeout(timeoutMs, TimeUnit.MILLISECONDS); // Useful for debugging testing 
	    connect(req);
	}
	
	private void connect(DeviceRequest req) throws EventException, InterruptedException {
		req = requester.post(req);
		info = (DeviceInformation<M>)req.getDeviceInformation();
		this.name = info.getName();
	}
	
	public void disconnect() throws EventException {
		requester.disconnect(); // Requester can still be used again after a disconnect
	}

	public String getName() {
		if (info==null) update();
		return info.getName();
	}

	public void setName(String name) {
		// TODO
		throw new RuntimeException("Devices may not have this changed remotely currently!");
	}

	public void setLevel(int level) {
		// TODO
		throw new RuntimeException("Devices may not have this changed remotely currently!");
	}

	public int getLevel() {
		if (info==null) update();
		return info.getLevel();
	}

	protected abstract DeviceRequest update();

}
