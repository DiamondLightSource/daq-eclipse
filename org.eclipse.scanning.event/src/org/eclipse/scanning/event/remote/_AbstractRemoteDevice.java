package org.eclipse.scanning.event.remote;

import java.net.URI;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceRequest;

abstract class _AbstractRemoteDevice<M> extends AbstractRemoteService {

	
	protected DeviceInformation<M>            info;
	protected final IRequester<DeviceRequest> requester;

	private _AbstractRemoteDevice(URI uri, IEventService eservice) throws EventException {
		requester = eservice.createRequestor(uri, EventConstants.DEVICE_REQUEST_TOPIC, EventConstants.DEVICE_RESPONSE_TOPIC);
	    requester.setTimeout(RemoteServiceFactory.getTime(), RemoteServiceFactory.getTimeUnit()); // Useful for debugging testing 
	}

	@SuppressWarnings("unchecked")
	_AbstractRemoteDevice(DeviceRequest req, URI uri, IEventService eservice) throws EventException, InterruptedException {
		this(uri, eservice);
		req = requester.post(req);
		info = (DeviceInformation<M>)req.getDeviceInformation();
	}
	
	public void disconnect() throws EventException {
		requester.disconnect();
	}

	public String getName() {
		update();
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
		update();
		return info.getLevel();
	}

	protected abstract DeviceRequest update();

}
