package org.eclipse.scanning.api.scan;

import java.net.InetAddress;
import java.util.UUID;

import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.points.IPosition;

public abstract class AbstractRunnableDevice<T> implements IRunnableDevice<T> {

	private   String                     scanId;
	private   int                        level = 1;
	private   String                     name;
	protected IScanningService           scanningService;
	protected IDeviceConnectorService    deviceService;
	private   DeviceState                state;
	private   IPublisher<ScanBean>       publisher;

	protected AbstractRunnableDevice() {
        this(null);
	}
	protected AbstractRunnableDevice(IPublisher<ScanBean> publisher) {
		this.publisher = publisher;
		this.scanId    = UUID.randomUUID().toString();
	}

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

	public DeviceState getState() {
		return state;
	}

	public void setState(DeviceState nstate) throws ScanningException {
		setState(nstate, null);
	}

	/**
	 * 
	 * @param nstate
	 * @param position
	 * @throws ScanningException 
	 */
	public void setState(DeviceState nstate, IPosition position) throws ScanningException {
		try {
			final ScanBean sent = new ScanBean();
			sent.setDeviceName(getName());
			sent.setDeviceState(nstate);
			sent.setPreviousDeviceState(state);
			sent.setPosition(position);
			sent.setUniqueId(getScanId());
			sent.setHostName(InetAddress.getLocalHost().getHostName());
			
			this.state = nstate;
			
			if (publisher!=null) publisher.broadcast(sent);

		} catch (Exception ne) {
			if (ne instanceof ScanningException) throw (ScanningException)ne;
			throw new ScanningException(this, ne);
		}
		
	}

	public String getScanId() {
		return scanId;
	}

	public void setScanId(String scanId) {
		this.scanId = scanId;
	}
	public IPublisher<ScanBean> getPublisher() {
		return publisher;
	}
	public void setPublisher(IPublisher<ScanBean> publisher) {
		this.publisher = publisher;
	}


}
