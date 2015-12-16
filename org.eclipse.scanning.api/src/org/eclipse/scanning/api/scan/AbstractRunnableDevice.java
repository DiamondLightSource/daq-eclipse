package org.eclipse.scanning.api.scan;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPosition;

/**
 * @see IRunnableDevice
 * @author fcp94556
 *
 * @param <T>
 */
public abstract class AbstractRunnableDevice<T> implements IRunnableDevice<T> {

	private   String                     scanId;
	private   int                        level = 1;
	private   String                     name;
	protected IScanningService           scanningService;
	protected IDeviceConnectorService    deviceService;
	private   DeviceState                state = DeviceState.IDLE;
	private   IPublisher<ScanBean>       publisher;
	private   ScanBean                   bean;

	protected AbstractRunnableDevice() {
		this.scanId    = UUID.randomUUID().toString();
	}

	public ScanBean getBean() {
		return bean;
	}
	public void setBean(ScanBean bean) throws ScanningException {
		this.bean = bean;
		bean.setDeviceState(state);
		try {
			bean.setHostName(InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			throw new ScanningException("Unable to read name of host!");
		}
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
	protected void setState(DeviceState nstate, IPosition position) throws ScanningException {
		try {
			// The bean must be set in order to change state.
			bean.setDeviceName(getName());
			bean.setDeviceState(nstate);
			bean.setPreviousDeviceState(state);
			bean.setPosition(position);
			
			this.state = nstate;
			
			if (publisher!=null) publisher.broadcast(bean);

		} catch (Exception ne) {
			if (ne instanceof ScanningException) throw (ScanningException)ne;
			throw new ScanningException(this, ne);
		}
		
	}

	protected void positionComplete(IPosition pos, int count, int size) throws GeneratorException, EventException {
		
		if (publisher==null) return;
		final ScanBean bean = getBean();
		bean.setPoint(count);
		bean.setPosition(pos);
		bean.setPreviousDeviceState(bean.getDeviceState());
		if (size>-1) bean.setPercentComplete((double)count/size);
		
		publisher.broadcast(bean);
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
