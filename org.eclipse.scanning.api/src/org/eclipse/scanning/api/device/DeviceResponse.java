package org.eclipse.scanning.api.device;

import java.util.Collection;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IResponseProcess;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.eclipse.scanning.api.scan.ScanningException;

public class DeviceResponse implements IResponseProcess<DeviceRequest> {

	private IDeviceService            dservice;
	private DeviceRequest             bean;
	private IPublisher<DeviceRequest> publisher;

	public DeviceResponse(IDeviceService dservice, DeviceRequest bean, IPublisher<DeviceRequest> statusNotifier) {
		this.dservice = dservice;
		this.bean     = bean;
		this.publisher = statusNotifier;
	}

	@Override
	public DeviceRequest getBean() {
		return bean;
	}

	@Override
	public IPublisher<DeviceRequest> getPublisher() {
		return publisher;
	}

	@Override
	public DeviceRequest process(DeviceRequest request) throws EventException {
		try {
			final Collection<String> names = dservice.getRunnableDeviceNames();
			for (String name : names) {
				
				if (request.getDeviceName()!=null && !name.matches(request.getDeviceName())) continue;
				
				IRunnableDevice<Object> device = dservice.getRunnableDevice(name);
				if (device==null) throw new EventException("There is no created device called '"+name+"'");
				if (!(device instanceof AbstractRunnableDevice)) continue;
				
				if (request.getDeviceModel()!=null) {
					device.configure(request.getDeviceModel());
				}
				
				DeviceInformation<?> info = ((AbstractRunnableDevice<?>)device).getDeviceInformation();
				request.addDeviceInformation(info);
			}
			return request;
		} catch (ScanningException ne) {
			throw new EventException("", ne);
		}
	}

}
