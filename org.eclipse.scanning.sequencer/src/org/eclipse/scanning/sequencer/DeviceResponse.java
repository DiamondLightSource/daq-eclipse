package org.eclipse.scanning.sequencer;

import java.util.Collection;

import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IDeviceService;
import org.eclipse.scanning.api.device.IRunnableDevice;
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

	public DeviceResponse(DeviceRequest bean, IPublisher<DeviceRequest> statusNotifier) {
		this(new DeviceServiceImpl(), bean, statusNotifier); // TODO Should we use OSGi for the service def?
	}

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
				IRunnableDevice<?> device = dservice.getRunnableDevice(name);
				if (!(device instanceof AbstractRunnableDevice)) continue;
				DeviceInformation info = ((AbstractRunnableDevice)device).getDeviceInformation();
				request.addDeviceInformation(info);
			}
			return request;
		} catch (ScanningException ne) {
			throw new EventException("", ne);
		}
	}

}
