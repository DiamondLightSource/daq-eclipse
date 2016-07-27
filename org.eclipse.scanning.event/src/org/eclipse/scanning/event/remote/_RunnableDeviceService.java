package org.eclipse.scanning.event.remote;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class _RunnableDeviceService extends AbstractRemoteService<IRunnableDeviceService> implements IRunnableDeviceService {
	
	private static final Logger logger = LoggerFactory.getLogger(_RunnableDeviceService.class);

	private IRequester<DeviceRequest> requester;
	private IScannableDeviceService   cservice;
	
	public void init() throws EventException {
		requester = eservice.createRequestor(uri, IEventService.DEVICE_REQUEST_TOPIC, IEventService.DEVICE_RESPONSE_TOPIC);
	    requester.setTimeout(RemoteServiceFactory.getTime(), RemoteServiceFactory.getTimeUnit()); // Useful for debugging testing 
	}

	@Override
	public IPositioner createPositioner() throws ScanningException {
		try {
			return new _Positioner(uri, eservice);
		} catch (EventException e) {
			throw new ScanningException("Cannot create a positioner!", e);
		}
	}
	
	@Override
	public void disconnect() throws EventException {
		requester.disconnect();
	}

	@Override
	public <T> IRunnableDevice<T> createRunnableDevice(T model) throws ScanningException {
		return createRunnableDevice(model, true);
	}

	@Override
	public <T> IRunnableDevice<T> createRunnableDevice(T model, boolean configure) throws ScanningException {
		try {
			return new _RunnableDevice(new DeviceRequest(model, configure), uri, eservice);
		} catch (EventException | InterruptedException e) {
			throw new ScanningException(e);
		}
	}

	@Override
	public <T> IRunnableDevice<T> createRunnableDevice(T model, IPublisher<ScanBean> publisher) throws ScanningException {
		throw new ScanningException("Not possible to set custom publishers on "+getClass().getSimpleName()+" because it is remote!");
	}

	@Override
	public <T> IRunnableDevice<T> getRunnableDevice(String name) throws ScanningException {
		return getRunnableDevice(name, null);
	}

	@Override
	public <T> IRunnableDevice<T> getRunnableDevice(String name, IPublisher<ScanBean> publisher) throws ScanningException {
		try {
			return new _RunnableDevice(new DeviceRequest(name), uri, eservice);
		} catch (EventException | InterruptedException e) {
			throw new ScanningException(e);
		}
	}

	@Override
	public Collection<String> getRunnableDeviceNames() throws ScanningException {
		@SuppressWarnings("rawtypes")
		DeviceInformation[] devices = getDevices();
	    String[] names = new String[devices.length];
	    for (int i = 0; i < devices.length; i++) names[i] = devices[i].getName();
		return Arrays.asList(names);
	}

	@Override
	public IScannableDeviceService getDeviceConnectorService() {
		if (cservice == null) {
			try {
				cservice = RemoteServiceFactory.getRemoteService(uri, IScannableDeviceService.class, eservice);
			} catch (InstantiationException | IllegalAccessException | EventException e) {
				logger.error("Cannot get service!", e);
			}
		}
		return cservice;
	}

	private DeviceInformation<?>[] getDevices() throws ScanningException {
	    DeviceRequest req;
		try {
			req = requester.post(new DeviceRequest());
		} catch (EventException | InterruptedException e) {
			throw new ScanningException("Cannot get devices! Connection to borker may be lost or no server up!", e);
		}
	    return req.getDevices().toArray(new DeviceInformation<?>[req.size()]);
	}

	@Override
	public Collection<DeviceInformation<?>> getDeviceInformation() throws ScanningException {
		return Arrays.asList(getDevices());
	}
}
