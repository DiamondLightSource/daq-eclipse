package org.eclipse.malcolm.core;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.eclipse.malcolm.api.MalcolmDeviceException;
import org.eclipse.malcolm.api.State;
import org.eclipse.malcolm.api.connector.IMalcolmConnectorService;
import org.eclipse.malcolm.api.event.IMalcolmListener;
import org.eclipse.malcolm.api.event.MalcolmEvent;
import org.eclipse.malcolm.api.event.MalcolmEventBean;
import org.eclipse.malcolm.api.message.JsonMessage;

public class MalcolmEventDelegate {
	
	private String          topicName;
	
	// JMS things, these are null when not running and 
	// are cleaned up at the end of a run.
	private MessageProducer producer;
	private Connection      connection;
	private Session         session;
	private URI             uri;

	// listeners
	private Collection<IMalcolmListener<MalcolmEventBean>> listeners;
	
	// Bean to contain all the settings for a given
	// scan and to hold data for scan events
	private MalcolmEventBean templateBean;

	private IMalcolmConnectorService<JsonMessage> service;

	public MalcolmEventDelegate(URI uri, String deviceName, IMalcolmConnectorService<JsonMessage> service) {
		
		this.uri     = uri;
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
		
		try {
			if (producer==null) producer = createProducer();
			
			if (templateBean!=null) BeanMerge.merge(templateBean, event);
			
			
			final String json = service.marshal(event);
			
			TextMessage temp = session.createTextMessage(json);
			producer.send(temp, DeliveryMode.NON_PERSISTENT, 1, 1000);
			fireMalcolmListeners(event);
			
		} catch (Exception ne) {
			producer = null;
		    connection.close();
			throw ne;
		}
	}


	private MessageProducer createProducer() throws JMSException {
		
		ConnectionFactory connectionFactory = (ConnectionFactory)service.createConnectionFactory(uri);		
		connection = connectionFactory.createConnection();

		this.session      = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		final Topic topic = session.createTopic(getTopicName());
 	    this.producer     = session.createProducer(topic);
		connection.start();
		
		return producer;
	}

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public void close() throws Exception {
		try {
			if (producer!=null) producer.close();
			if (connection!=null) connection.close();
			if (session!=null) session.close();
			if (listeners!=null) listeners.clear();
			
		} finally {
			producer = null;
			connection = null;
			session = null;
			listeners = null;
		}
	}
	
	public URI getURI() {
		return uri;
	}

	public URI setURI(URI uri) throws MalcolmDeviceException {
		if (producer!=null) throw new MalcolmDeviceException("The device is already connected, please call 'close()' or 'absort()' to stop the run.");
		URI old = this.uri;
		this.uri = uri;
		return old;
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

}
