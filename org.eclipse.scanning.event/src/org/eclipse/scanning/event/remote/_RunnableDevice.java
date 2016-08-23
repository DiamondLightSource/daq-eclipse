package org.eclipse.scanning.event.remote;

import java.net.URI;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.DeviceAction;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class _RunnableDevice<M> extends _AbstractRemoteDevice<M> implements IRunnableDevice<M> {
	
	private final static Logger logger = LoggerFactory.getLogger(_RunnableDevice.class);

	_RunnableDevice(DeviceRequest req, URI uri, IEventService eservice) throws EventException, InterruptedException {
		super(req, uri, eservice);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void configure(M model) throws ScanningException {
		try {
			DeviceRequest req = requester.post(new DeviceRequest(info.getName(), model));
			info = (DeviceInformation<M>)req.getDeviceInformation();
		} catch (Exception ne) {
			throw new ScanningException(ne);
		}
	}

	@Override
	public M getModel() {
		update();
		return info.getModel();
	}
	
	@Override
	public void validate(M model) throws Exception {
		DeviceRequest res = requester.post(new DeviceRequest(info.getName(), DeviceAction.VALIDATE, model));		
		if (res.getErrorMessage()!=null) {
			if (res.getErrorFieldNames()!=null) {
				throw new ModelValidationException(res.getErrorMessage(), model, res.getErrorFieldNames());
			} else {
				throw new Exception(res.getErrorMessage());
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public DeviceState getDeviceState() throws ScanningException {
		try {
			DeviceRequest req = requester.post(new DeviceRequest(info.getName()));
			info = (DeviceInformation<M>)req.getDeviceInformation();
		} catch (Exception ne) {
			throw new ScanningException(ne);
		}
		return info.getState();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected DeviceRequest update() {
		try {
			DeviceRequest req = requester.post(new DeviceRequest(info.getName()));
			info = (DeviceInformation<M>)req.getDeviceInformation();
			return req;
		} catch (Exception ne) {
			logger.error("Cannot update device info for "+info.getName(), ne);
			return null;
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
			info = (DeviceInformation<M>)req.getDeviceInformation();
		} catch (Exception ne) {
			throw new ScanningException(ne);
		}
	}

}
