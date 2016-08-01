package org.eclipse.scanning.event.remote;

import java.net.URI;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.annotation.ui.DeviceType;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.DeviceAction;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.eclipse.scanning.api.points.IPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class _Scannable<T> extends _AbstractRemoteDevice<T> implements IScannable<T> {

	private final static Logger logger = LoggerFactory.getLogger(_Scannable.class);

	_Scannable(DeviceRequest req, URI uri, IEventService eservice) throws EventException, InterruptedException {
		super(req, uri, eservice);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getPosition() throws Exception {
		DeviceRequest req = update();
		return (T)req.getDeviceValue();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setPosition(T value, IPosition position) throws Exception {
		try {
			DeviceRequest req = new DeviceRequest(info.getName(), DeviceType.SCANNABLE);
			req.setDeviceAction(DeviceAction.SET);
			req.setDeviceValue(value);
			req.setPosition(position);
			req = requester.post(req);
			info = (DeviceInformation<T>)req.getDeviceInformation();
		} catch (Exception ne) {
			logger.error("Cannot update device info for "+info.getName(), ne);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected DeviceRequest update() {
		try {
			DeviceRequest req = requester.post(new DeviceRequest(info.getName(), DeviceType.SCANNABLE));
			info = (DeviceInformation<T>)req.getDeviceInformation();
			return req;
		} catch (Exception ne) {
			logger.error("Cannot update device info for "+info.getName(), ne);
			return null;
		}
	}

	public String getUnit() {
		update();
		return info.getUnit();
	}
}
