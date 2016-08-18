package org.eclipse.scanning.event.remote;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.annotation.ui.DeviceType;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.DeviceAction;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.event.IPositionListenable;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class _Scannable<T> extends _AbstractRemoteDevice<T> implements IScannable<T>, IPositionListenable {

	private final static Logger logger = LoggerFactory.getLogger(_Scannable.class);

	_Scannable(DeviceRequest req, URI uri, IEventService eservice) throws EventException, InterruptedException {
		super(req, uri, eservice);
	    requester.setTimeout(25, TimeUnit.MILLISECONDS); /// Short scannables should be fast to get value from!
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
		if (info==null) update();  // We assume that they do not change unit.
		return info.getUnit();
	}
	
	
	/**
	 * Set the current upper limit, returning the previous value, if any.
	 * @param upper
	 * @return
	 */
	@Override
	@SuppressWarnings("unchecked")
	public T getMaximum() {
		if (info==null) update();
		return (T)info.getUpper();
	}

	/**
	 * Set the current lower limit, returning the previous value, if any.
	 * @param lower
	 * @return
	 */
	@Override
	@SuppressWarnings("unchecked")
	public T getMinimum() {
		if (info==null) update();
		return (T)info.getLower();
	}

	@Override
	public void addPositionListener(IPositionListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removePositionListener(IPositionListener listener) {
		// TODO Auto-generated method stub
		
	}
	

}
