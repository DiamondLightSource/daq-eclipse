package org.eclipse.scanning.event.remote;

import java.util.Arrays;
import java.util.List;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.annotation.ui.DeviceType;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.eclipse.scanning.api.scan.ScanningException;

class _ScannableDeviceService extends AbstractRemoteService implements IScannableDeviceService {

	private IRequester<DeviceRequest> requester;
	
	public void init() throws EventException {
		requester = eservice.createRequestor(uri, IEventService.DEVICE_REQUEST_TOPIC, IEventService.DEVICE_RESPONSE_TOPIC);
	    requester.setTimeout(RemoteServiceFactory.getTime(), RemoteServiceFactory.getTimeUnit()); // Useful for debugging testing 
	}
	
	@Override
	public void disconnect() throws EventException {
		requester.disconnect();
	}
	
	@Override
	public List<String> getScannableNames() throws ScanningException {
		
		DeviceInformation<?>[] devices = getDevices();
	    String[] names = new String[devices.length];
	    for (int i = 0; i < devices.length; i++) names[i] = devices[i].getName();
		return Arrays.asList(names);
	}

	private DeviceInformation<?>[] getDevices() throws ScanningException {
	    DeviceRequest req;
		try {
			req = requester.post(new DeviceRequest(DeviceType.SCANNABLE));
		} catch (EventException | InterruptedException e) {
			throw new ScanningException("Cannot get devices! Connection to borker may be lost or no server up!", e);
		}
	    return req.getDevices().toArray(new DeviceInformation<?>[req.size()]);
	}

	@Override
	public <T> IScannable<T> getScannable(String name) throws ScanningException {
		try {
			return new _Scannable<T>(new DeviceRequest(name, DeviceType.SCANNABLE), uri, eservice);
		} catch (EventException | InterruptedException e) {
			throw new ScanningException(e);
		}
	}

}
