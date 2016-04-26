package org.eclipse.scanning.test.event.queues.mocks;

import java.util.Collection;

import org.eclipse.scanning.api.device.IDeviceService;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositioner;

public class MockScanService implements IDeviceService {
	
	private IPositioner poser = new MockPositioner();

	@Override
	public IPositioner createPositioner() throws ScanningException {
		return poser;
	}

	@Override
	public <T> IRunnableDevice<T> createRunnableDevice(T model)
			throws ScanningException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> IRunnableDevice<T> createRunnableDevice(T model,
			boolean configure) throws ScanningException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> IRunnableDevice<T> createRunnableDevice(T model,
			IPublisher<ScanBean> publisher) throws ScanningException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> IRunnableDevice<T> getRunnableDevice(String name)
			throws ScanningException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> IRunnableDevice<T> getRunnableDevice(String name,
			IPublisher<ScanBean> publisher) throws ScanningException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getRunnableDeviceNames() throws ScanningException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Class<?>> getRunnableDeviceModels()
			throws ScanningException {
		// TODO Auto-generated method stub
		return null;
	}

	public IPositioner getPositioner() {
		return poser;
	}

}
