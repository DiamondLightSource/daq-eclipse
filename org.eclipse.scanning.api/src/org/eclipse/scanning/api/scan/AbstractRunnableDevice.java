package org.eclipse.scanning.api.scan;

public abstract class AbstractRunnableDevice<T> implements IRunnableDevice<T> {

	private int    level = 1;
	private String name;
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

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


}
