package org.eclipse.scanning.event;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AbstractConnection {
	
	protected static Logger logger = LoggerFactory.getLogger(AbstractConnection.class);

	protected final URI              uri;
	protected String                 topicName;
	
	protected String                 submitQueueName;
	protected String                 statusQueueName;
	protected String                 statusTopicName;
	protected String                 killTopicName;

	protected IEventConnectorService service;
	
	protected QueueConnection  connection;
	protected QueueSession     qSession;
	protected Session          session;

	AbstractConnection(URI uri, String topic, IEventConnectorService service) {
		this.uri = uri;
		this.topicName = topic;
		this.service = service;
	}
	
	AbstractConnection(URI uri, String submitQName, String statusQName, String statusTName, String terminateTName, IEventConnectorService service) {
		this.uri = uri;
		this.submitQueueName = submitQName;
		this.statusQueueName = statusQName;
		this.statusTopicName = statusTName;
		this.killTopicName = terminateTName;
		this.service = service;
	}
	
	protected Topic createTopic(String topicName) throws JMSException {
		
		if (connection==null) createConnection();
		if (session == null)  createSession();
		
		return session.createTopic(topicName);
	}
	
	protected Queue createQueue(String queueName) throws JMSException {
		
		if (connection==null) createConnection();
		if (qSession == null) createQSession();
		
		return qSession.createQueue(queueName);
	}

	
	private void createSession() throws JMSException {
		this.session      = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	}
	
	private void createQSession() throws JMSException {
		this.qSession     = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
	}

	private void createConnection() throws JMSException {
		Object factory = service.createConnectionFactory(uri);
		QueueConnectionFactory connectionFactory = (QueueConnectionFactory)factory;		
		this.connection = connectionFactory.createQueueConnection();
		connection.start();
	}

	public void disconnect() throws EventException {
		try {
			if (connection!=null)        connection.close();
			if (session!=null)           session.close();
			if (qSession!=null)          qSession.close();
			
		} catch (JMSException ne) {
			throw new EventException("Internal error - unable to close connection!", ne);
		
		} finally {
			connection = null;
			session = null;
		}
	}


	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topic) throws EventException {
		if (!isListenersEmpty()) throw new EventException("Cannot change the topic while listeners are still added! They would not function correctly.");
		this.topicName = topic;
		disconnect();
	}

	private boolean isListenersEmpty() {
		return true;
	}


	public URI getUri() {
		return uri;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((statusQueueName == null) ? 0 : statusQueueName.hashCode());
		result = prime * result
				+ ((submitQueueName == null) ? 0 : submitQueueName.hashCode());
		result = prime * result
				+ ((topicName == null) ? 0 : topicName.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractConnection other = (AbstractConnection) obj;
		if (statusQueueName == null) {
			if (other.statusQueueName != null)
				return false;
		} else if (!statusQueueName.equals(other.statusQueueName))
			return false;
		if (submitQueueName == null) {
			if (other.submitQueueName != null)
				return false;
		} else if (!submitQueueName.equals(other.submitQueueName))
			return false;
		if (topicName == null) {
			if (other.topicName != null)
				return false;
		} else if (!topicName.equals(other.topicName))
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	public String getSubmitQueueName() {
		return submitQueueName;
	}

	public void setSubmitQueueName(String submitQueueName) {
		this.submitQueueName = submitQueueName;
	}

	public String getStatusQueueName() {
		return statusQueueName;
	}

	public void setStatusQueueName(String statusQueueName) {
		this.statusQueueName = statusQueueName;
	}

	public String getStatusTopicName() {
		return statusTopicName;
	}

	public void setStatusTopicName(String statusTopicName) {
		this.statusTopicName = statusTopicName;
	}

	public String getKillTopicName() {
		return killTopicName;
	}

	public void setKillTopicName(String terminateTopicName) {
		this.killTopicName = terminateTopicName;
	}

}
