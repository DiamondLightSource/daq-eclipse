package org.eclipse.scanning.event.remote;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.scan.DeviceAction;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class _RunnableDevice<T> extends AbstractRemoteService<IRunnableDevice<T>> implements IRunnableDevice<T> {
	
	private final static Logger logger = LoggerFactory.getLogger(_RunnableDevice.class);
	
	private DeviceInformation<T>            info;
	private final IRequester<DeviceRequest> requester;

	private _RunnableDevice(URI uri, IEventService eservice) throws EventException {
		requester = eservice.createRequestor(uri, EventConstants.DEVICE_REQUEST_TOPIC, EventConstants.DEVICE_RESPONSE_TOPIC);
	    requester.setTimeout(RemoteServiceFactory.getTime(), RemoteServiceFactory.getTimeUnit()); // Useful for debugging testing 
	}

	_RunnableDevice(DeviceRequest req, URI uri, IEventService eservice) throws EventException, InterruptedException {
		this(uri, eservice);
		req = requester.post(req);
		info = (DeviceInformation<T>)req.getDeviceInformation();
	}

	@Override
	public String getName() {
		update();
		return info.getName();
	}

	@Override
	public void setName(String name) {
		throw new RuntimeException("Devices may not have this changed remotely currently!");
	}

	@Override
	public void setLevel(int level) {
		throw new RuntimeException("Devices may not have this changed remotely currently!");
	}

	@Override
	public int getLevel() {
		update();
		return info.getLevel();
	}

	@Override
	public void configure(T model) throws ScanningException {
		try {
			DeviceRequest req = requester.post(new DeviceRequest(info.getName(), model));
			info = (DeviceInformation<T>)req.getDeviceInformation();
		} catch (Exception ne) {
			throw new ScanningException(ne);
		}
	}

	@Override
	public T getModel() {
		update();
		return info.getModel();
	}
	
	@Override
	public DeviceState getDeviceState() throws ScanningException {
		try {
			DeviceRequest req = requester.post(new DeviceRequest(info.getName()));
			info = (DeviceInformation<T>)req.getDeviceInformation();
		} catch (Exception ne) {
			throw new ScanningException(ne);
		}
		return info.getState();
	}

	private void update() {
		try {
			DeviceRequest req = requester.post(new DeviceRequest(info.getName()));
			info = (DeviceInformation<T>)req.getDeviceInformation();
		} catch (Exception ne) {
			logger.error("Cannot update device info for "+info.getName(), ne);
		}
	}

	@Override
	public void run(IPosition position) throws ScanningException, InterruptedException {
		DeviceRequest req = new DeviceRequest(info.getName(), DeviceAction.RUN);
		req.setPosition(position);
		method(req);
	}

	@Override
	public void reset() throws ScanningException {
		method(new DeviceRequest(info.getName(), DeviceAction.RESET));
	}

	@Override
	public void abort() throws ScanningException {
		method(new DeviceRequest(info.getName(), DeviceAction.ABORT));
	}

	private void method(DeviceRequest deviceRequest) throws ScanningException {
		try {
			DeviceRequest req = requester.post(deviceRequest);
			info = (DeviceInformation<T>)req.getDeviceInformation();
		} catch (Exception ne) {
			throw new ScanningException(ne);
		}
	}

}
