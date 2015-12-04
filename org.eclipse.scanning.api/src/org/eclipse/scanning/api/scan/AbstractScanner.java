package org.eclipse.scanning.api.scan;

public abstract class AbstractScanner<T> implements IRunnableDevice<T> {

	
	protected IScanningService  scanningService;
	protected IDeviceConnectorService    deviceService;


	public IScanningService getScanningService() {
		return scanningService;
	}

	public void setScanningService(IScanningService scanningService) {
		this.scanningService = scanningService;
	}

	public IDeviceConnectorService getDeviceService() {
		return deviceService;
	}

	public void setDeviceService(IDeviceConnectorService hardwareservice) {
		this.deviceService = hardwareservice;
	}


}
