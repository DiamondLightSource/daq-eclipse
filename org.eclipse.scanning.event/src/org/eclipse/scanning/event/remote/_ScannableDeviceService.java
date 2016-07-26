package org.eclipse.scanning.event.remote;

import java.util.List;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.scan.ScanningException;

class _ScannableDeviceService extends AbstractRemoteService<IScannableDeviceService> implements IScannableDeviceService {

	@Override
	public List<String> getScannableNames() throws ScanningException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> IScannable<T> getScannable(String name) throws ScanningException {
		// TODO Auto-generated method stub
		return null;
	}

}
