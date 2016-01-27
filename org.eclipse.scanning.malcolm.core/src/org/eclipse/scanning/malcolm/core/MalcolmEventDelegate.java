package org.eclipse.scanning.malcolm.core;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnectorService;
import org.eclipse.scanning.api.malcolm.event.IMalcolmListener;
import org.eclipse.scanning.api.malcolm.event.MalcolmEvent;
import org.eclipse.scanning.api.malcolm.event.MalcolmEventBean;
import org.eclipse.scanning.api.malcolm.message.JsonMessage;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.IRunnableDevice;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;

public class MalcolmEventDelegate {
	
	private String          topicName;
	
	// listeners
	private Collection<IMalcolmListener<MalcolmEventBean>> mlisteners;
	private Collection<IRunListener>                       rlisteners;
	
	// Bean to contain all the settings for a given
	// scan and to hold data for scan events
	private MalcolmEventBean templateBean;

	private IMalcolmConnectorService<JsonMessage> service;

	public MalcolmEventDelegate(String deviceName, IMalcolmConnectorService<JsonMessage> service) {
		
		this.service = service;
		
		String beamline = System.getenv("BEAMLINE");
		if (beamline == null) beamline = "test";
		
		topicName = "malcolm.topic."+beamline+"."+deviceName;
	}

    /**
     * Call to publish an event. If the topic is not opened, this
     * call prompts the delegate to open a connection. After this
     * the close method *must* be called.
     * 
     * @param event
     * @throws Exception
     */
	public void sendEvent(MalcolmEventBean event)  throws Exception {

		if (templateBean!=null) BeanMerge.merge(templateBean, event);
		fireMalcolmListeners(event);
	}


	public void addMalcolmListener(IMalcolmListener<MalcolmEventBean> l) {
		if (mlisteners==null) mlisteners = Collections.synchronizedCollection(new LinkedHashSet<IMalcolmListener<MalcolmEventBean>>());
		mlisteners.add(l);
	}
	
	public void removeMalcolmListener(IMalcolmListener<MalcolmEventBean> l) {
		if (mlisteners==null) return;
		mlisteners.remove(l);
	}
	
	private void fireMalcolmListeners(MalcolmEventBean message) {
		
		if (mlisteners==null) return;
		
		// Make array, avoid multi-threading issues.
		final IMalcolmListener<MalcolmEventBean>[] la = mlisteners.toArray(new IMalcolmListener[mlisteners.size()]);
		final MalcolmEvent<MalcolmEventBean> evt = new MalcolmEvent<MalcolmEventBean>(message);
		for (IMalcolmListener<MalcolmEventBean> l : la) l.eventPerformed(evt);
	}

	public void sendStateChanged(DeviceState state, DeviceState old, String message) throws Exception {
		final MalcolmEventBean evt = new MalcolmEventBean();
		evt.setPreviousState(old);
		evt.setState(state);
		evt.setMessage(message);
		sendEvent(evt);
	}

	public void setTemplateBean(MalcolmEventBean bean) {
		templateBean = bean;
	}

	public void close() {
		if (mlisteners!=null) mlisteners.clear();
	}

	public void addRunListener(IRunListener l) {
		if (rlisteners==null) rlisteners = Collections.synchronizedCollection(new LinkedHashSet<IRunListener>());
		rlisteners.add(l);
	}
	
	public void removeRunListener(IRunListener l) {
		if (rlisteners==null) return;
		rlisteners.remove(l);
	}
	
	protected void fireRunWillPerform(IRunnableDevice<?> device, IPosition position) throws ScanningException{
		
		if (rlisteners==null) return;
		
		final RunEvent evt = new RunEvent(device, position);
		
		// Make array, avoid multi-threading issues.
		final IRunListener[] la = rlisteners.toArray(new IRunListener[rlisteners.size()]);
		for (IRunListener l : la) l.runWillPerform(evt);
	}
	
	protected void fireRunPerformed(IRunnableDevice<?> device, IPosition position) throws ScanningException{
		
		if (rlisteners==null) return;
		
		final RunEvent evt = new RunEvent(device, position);
		
		// Make array, avoid multi-threading issues.
		final IRunListener[] la = rlisteners.toArray(new IRunListener[rlisteners.size()]);
		for (IRunListener l : la) l.runPerformed(evt);
	}
	
	protected void fireWriteWillPerform(IRunnableDevice<?> device, IPosition position) throws ScanningException{
		
		if (rlisteners==null) return;
		
		final RunEvent evt = new RunEvent(device, position);
		
		// Make array, avoid multi-threading issues.
		final IRunListener[] la = rlisteners.toArray(new IRunListener[rlisteners.size()]);
		for (IRunListener l : la) l.writeWillPerform(evt);
	}
	
	protected void fireWritePerformed(IRunnableDevice<?> device, IPosition position) throws ScanningException{
		
		if (rlisteners==null) return;
		
		final RunEvent evt = new RunEvent(device, position);
		
		// Make array, avoid multi-threading issues.
		final IRunListener[] la = rlisteners.toArray(new IRunListener[rlisteners.size()]);
		for (IRunListener l : la) l.writePerformed(evt);
	}


}
