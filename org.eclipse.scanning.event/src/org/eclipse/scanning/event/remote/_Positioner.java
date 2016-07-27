package org.eclipse.scanning.event.remote;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.scan.PositionRequestType;
import org.eclipse.scanning.api.event.scan.PositionerRequest;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.event.IPositioner;

class _Positioner extends AbstractRemoteService<IPositioner> implements IPositioner {

	private IRequester<PositionerRequest> requester;
	
	// We use the uniqueid of the request to get a kind of 'session'
	private PositionerRequest             request;

	public _Positioner(URI uri, IEventService eservice) throws EventException {
		setUri(uri);
		setEventService(eservice);
		init();
	}
	
	@Override
	public void disconnect() throws EventException {
		requester.disconnect();
	}
	
	public void init()  throws EventException {
		requester = eservice.createRequestor(uri, EventConstants.POSITIONER_REQUEST_TOPIC, EventConstants.POSITIONER_RESPONSE_TOPIC);
	    requester.setTimeout(RemoteServiceFactory.getTime(), RemoteServiceFactory.getTimeUnit()); // Useful for debugging testing 
		request   = new PositionerRequest();
	}

	@Override
	public void addPositionListener(IPositionListener listener) {
		// TODO If/When this is required, modify PositionDelegate to send topic events, listen to those
        throw new RuntimeException("Not implemented as yet!");
	}

	@Override
	public void removePositionListener(IPositionListener listener) {
		// TODO If/When this is required, modify PositionDelegate to send topic events, listen to those
        throw new RuntimeException("Not implemented as yet!");
	}

	@Override
	public boolean setPosition(IPosition position) throws ScanningException, InterruptedException {
		request.setPositionType(PositionRequestType.SET);
		request.setPosition(position);
		try {
			request = requester.post(request);
			return true;
		} catch (EventException e) {
			throw new ScanningException(e);
		}
	}

	@Override
	public IPosition getPosition() throws ScanningException {
		request.setPositionType(PositionRequestType.GET);
		request.setPosition(null);
		try {
			request = requester.post(request);
		    return request.getPosition();
		} catch (EventException | InterruptedException e) {
			throw new ScanningException(e);
		}
	}

	@Override
	public List<IScannable<?>> getMonitors() throws ScanningException {
		// TODO Use the _Scannable which is a remote scannable connection.
		return null;
	}

	@Override
	public void setMonitors(List<IScannable<?>> monitors) throws ScanningException {
		// TODO Use the _Scannable which is a remote scannable connection.
		throw new ScanningException("Monitors may not be set on a remote positioner!");
	}

	@Override
	public void setMonitors(IScannable<?>... monitors) throws ScanningException {
		setMonitors(Arrays.asList(monitors));
	}

	@Override
	public void abort() {
		request.setPositionType(PositionRequestType.ABORT);
		try {
			request = requester.post(request);
		} catch (EventException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}
