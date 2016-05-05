package org.eclipse.scanning.api.device;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.scanning.api.IScanAttributeContainer;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;

/**
 * @see IRunnableDevice
 * @author Matthew Gerring
 *
 * @param <T>
 */
public abstract class AbstractRunnableDevice<T> implements IRunnableEventDevice<T>, IScanAttributeContainer {

	// Data
	protected T                          model;
	private   String                     name;
	private   int                        level = 1;
	private   String                     scanId;
	private   ScanBean                   bean;
	private   DeviceInformation<T>       deviceInformation;

	// OSGi services and intraprocess events
	protected IRunnableDeviceService             runnableDeviceService;
	protected IDeviceConnectorService    connectorService;
	private   IPublisher<ScanBean>       publisher;
	
	// Listeners
	private   Collection<IRunListener>   rlisteners;
	
	// Attributes
	private Map<String, Object>          scanAttributes;

	protected AbstractRunnableDevice() {
		this.scanId     = UUID.randomUUID().toString();
		this.scanAttributes = new HashMap<>();
	}

	public ScanBean getBean() {
		if (bean==null) bean = new ScanBean();
		return bean;
	}
	
	public void setBean(ScanBean bean) throws ScanningException {
		this.bean = bean;
		try {
			bean.setHostName(InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			throw new ScanningException("Unable to read name of host!");
		}
	}

	public IRunnableDeviceService getRunnableDeviceService() {
		return runnableDeviceService;
	}

	public void setRunnableDeviceService(IRunnableDeviceService runnableDeviceService) {
		this.runnableDeviceService = runnableDeviceService;
	}

	public IDeviceConnectorService getConnectorService() {
		return connectorService;
	}

	public void setConnectorService(IDeviceConnectorService connectorService) {
		this.connectorService = connectorService;
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

	
	public void setDeviceState(DeviceState nstate) throws ScanningException {
		setDeviceState(nstate, null);
	}
	
	public void reset() throws ScanningException {
		setDeviceState(DeviceState.IDLE);
	}
	
	public void start(final IPosition pos) throws ScanningException, InterruptedException {
		
		final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>(1));
		final Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					AbstractRunnableDevice.this.run(pos);
				} catch (ScanningException|InterruptedException e) {
					// If you add an exception type to this catch clause,
					// you must also add an "else if" clause for it inside
					// the "if (!exceptions.isEmpty())" conditional below.
					e.printStackTrace();
					exceptions.add(e);
				}
			}
		}, "Scan Runner Thread "+getName());
		thread.start();
		
		// We delay by 500ms just so that we can 
		// immediately throw any connection exceptions
		Thread.sleep(500);
		
		// Re-throw any exception from the thread.
		if (!exceptions.isEmpty()) {
			Throwable ex = exceptions.get(0);

			// We must manually match the possible exception types because Java
			// doesn't let us do List<Either<ScanningException, InterruptedException>>.
			if (ex.getClass() == ScanningException.class) {
				throw (ScanningException) ex;

			} else if (ex.getClass() == InterruptedException.class) {
				throw (InterruptedException) ex;

			} else {
				throw new IllegalStateException();
			}
		}
	}

	/**
	 * 
	 * @param nstate
	 * @param position
	 * @throws ScanningException 
	 */
	protected void setDeviceState(DeviceState nstate, IPosition position) throws ScanningException {
		try {
			// The bean must be set in order to change state.
			if (bean==null) bean = new ScanBean();
			bean.setDeviceName(getName());
			bean.setPreviousDeviceState(bean.getDeviceState());
			bean.setDeviceState(nstate);
			bean.setPosition(position);
			
			if (publisher!=null) publisher.broadcast(bean);

		} catch (Exception ne) {
			if (ne instanceof ScanningException) throw (ScanningException)ne;
			throw new ScanningException(this, ne);
		}
		
	}
	
	public DeviceState getDeviceState() throws ScanningException {
		if (bean==null) return null;
		return bean.getDeviceState();
	}

	protected void positionComplete(IPosition pos, int count, int size) throws EventException, ScanningException {
		final ScanBean bean = getBean();
		bean.setPoint(count);
		bean.setPosition(pos);
		bean.setPreviousDeviceState(bean.getDeviceState());
		if (size>-1) bean.setPercentComplete(((double)count/size)*100);

		if (publisher != null) {
			publisher.broadcast(bean);
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
		
		final RunEvent evt = new RunEvent(this, position, getDeviceState());
		
		// Make array, avoid multi-threading issues.
		final IRunListener[] la = rlisteners.toArray(new IRunListener[rlisteners.size()]);
		for (IRunListener l : la) l.runWillPerform(evt);
	}
	
	public void fireRunPerformed(IPosition position) throws ScanningException {
		
		if (rlisteners==null) return;
		
		final RunEvent evt = new RunEvent(this, position, getDeviceState());
		
		// Make array, avoid multi-threading issues.
		final IRunListener[] la = rlisteners.toArray(new IRunListener[rlisteners.size()]);
		for (IRunListener l : la) l.runPerformed(evt);
	}
	
	public void fireWriteWillPerform(IPosition position) throws ScanningException {
		
		if (rlisteners==null) return;
		
		final RunEvent evt = new RunEvent(this, position, getDeviceState());
		
		// Make array, avoid multi-threading issues.
		final IRunListener[] la = rlisteners.toArray(new IRunListener[rlisteners.size()]);
		for (IRunListener l : la) l.writeWillPerform(evt);
	}
	
	public void fireWritePerformed(IPosition position) throws ScanningException {
		
		if (rlisteners==null) return;
		
		final RunEvent evt = new RunEvent(this, position, getDeviceState());
		
		// Make array, avoid multi-threading issues.
		final IRunListener[] la = rlisteners.toArray(new IRunListener[rlisteners.size()]);
		for (IRunListener l : la) l.writePerformed(evt);
	}


	public T getModel() {
		return model;
	}

	public void setModel(T model) {
		this.model = model;
	}
	
	@Override
	public void configure(T model) throws ScanningException {
		this.model = model;
		setDeviceState(DeviceState.READY);
	}


	@Override
	public void abort() throws ScanningException {

	}


	@Override
	public void pause() throws ScanningException {

	}

	@Override
	public void resume() throws ScanningException {

	}

	/**
	 * 
	 * @return null if no attributes, otherwise collection of the names of the attributes set
	 */
	@Override
	public Set<String> getScanAttributeNames() {
		return scanAttributes.keySet();
	}

	/**
	 * Set any attribute the implementing classes may provide
	 * 
	 * @param attributeName
	 *            is the name of the attribute
	 * @param value
	 *            is the value of the attribute
	 * @throws DeviceException
	 *             if an attribute cannot be set
	 */
	@Override
	public <A> void setScanAttribute(String attributeName, A value) throws Exception {
		scanAttributes.put(attributeName, (A)value);
	}

	/**
	 * Get the value of the specified attribute
	 * 
	 * @param attributeName
	 *            is the name of the attribute
	 * @return the value of the attribute
	 * @throws DeviceException
	 *             if an attribute cannot be retrieved
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <A> A getScanAttribute(String attributeName) throws Exception {
		return (A)scanAttributes.get(attributeName);
	}

	public DeviceInformation<T> getDeviceInformation() throws ScanningException {
		deviceInformation.setModel(getModel());
		deviceInformation.setState(getDeviceState());
 		return deviceInformation;
	}

	public void setDeviceInformation(DeviceInformation<T> deviceInformation) {
		this.deviceInformation = deviceInformation;
	}
}
