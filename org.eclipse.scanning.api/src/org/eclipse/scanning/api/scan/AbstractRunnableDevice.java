package org.eclipse.scanning.api.scan;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.UUID;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;

/**
 * @see IRunnableDevice
 * @author fcp94556
 *
 * @param <T>
 */
public abstract class AbstractRunnableDevice<T> implements IRunnableEventDevice<T> {

	// Data
	private   T                          model;
	private   String                     name;
	private   int                        level = 1;
	private   String                     scanId;
	private   ScanBean                   bean;

	// OSGi services and intraprocess events
	protected IScanningService           scanningService;
	protected IDeviceConnectorService    deviceService;
	private   IPublisher<ScanBean>       publisher;

	// State
	private   DeviceState                state = DeviceState.IDLE;
	
	// Listeners
	private   Collection<IRunListener>   rlisteners;

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

	protected void positionComplete(IPosition pos, int count, int size) throws EventException, ScanningException {
		
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


	public void addRunListener(IRunListener l) {
		if (rlisteners==null) rlisteners = Collections.synchronizedCollection(new LinkedHashSet<IRunListener>());
		rlisteners.add(l);
	}
	
	public void removeRunListener(IRunListener l) {
		if (rlisteners==null) return;
		rlisteners.remove(l);
	}
	
	public void fireRunWillPerform(IPosition position) throws ScanningException {
		
		if (rlisteners==null) return;
		
		final RunEvent evt = new RunEvent(this, position);
		
		// Make array, avoid multi-threading issues.
		final IRunListener[] la = rlisteners.toArray(new IRunListener[rlisteners.size()]);
		for (IRunListener l : la) l.runWillPerform(evt);
	}
	
	public void fireRunPerformed(IPosition position) throws ScanningException {
		
		if (rlisteners==null) return;
		
		final RunEvent evt = new RunEvent(this, position);
		
		// Make array, avoid multi-threading issues.
		final IRunListener[] la = rlisteners.toArray(new IRunListener[rlisteners.size()]);
		for (IRunListener l : la) l.runPerformed(evt);
	}

	public T getModel() {
		return model;
	}

	public void setModel(T model) {
		this.model = model;
	}


}
