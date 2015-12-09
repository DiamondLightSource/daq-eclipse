package org.eclipse.scanning.malcolm.core;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

import org.eclipse.scanning.api.malcolm.State;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnectorService;
import org.eclipse.scanning.api.malcolm.event.IMalcolmListener;
import org.eclipse.scanning.api.malcolm.event.MalcolmEvent;
import org.eclipse.scanning.api.malcolm.event.MalcolmEventBean;
import org.eclipse.scanning.api.malcolm.message.JsonMessage;

public class MalcolmEventDelegate {
	
	private String          topicName;
	
	// listeners
	private Collection<IMalcolmListener<MalcolmEventBean>> listeners;
	
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
		if (listeners==null) listeners = Collections.synchronizedCollection(new LinkedHashSet<IMalcolmListener<MalcolmEventBean>>());
		listeners.add(l);
	}
	
	public void removeMalcolmListener(IMalcolmListener<MalcolmEventBean> l) {
		if (listeners==null) return;
		listeners.remove(l);
	}
	
	private void fireMalcolmListeners(MalcolmEventBean message) {
		
		if (listeners==null) return;
		
		// Make array, avoid multi-threading issues.
		final IMalcolmListener<MalcolmEventBean>[] la = listeners.toArray(new IMalcolmListener[listeners.size()]);
		final MalcolmEvent<MalcolmEventBean> evt = new MalcolmEvent<MalcolmEventBean>(message);
		for (IMalcolmListener<MalcolmEventBean> l : la) l.eventPerformed(evt);
	}

	public void sendStateChanged(State state, State old, String message) throws Exception {
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
		if (listeners!=null) listeners.clear();
	}

}
