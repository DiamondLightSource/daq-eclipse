package org.eclipse.scanning.event;

import java.net.URI;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;

class AbstractConnection {

	protected final URI              uri;
	protected String                 topicName;
	
	protected String                 submitQueueName;
	protected String                 statusQueueName;
	protected String                 statusTopicName;
	protected String                 terminateTopicName;

	protected IEventConnectorService service;
	
	protected Connection      connection;
	protected Session         session;

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
		this.terminateTopicName = terminateTName;
		this.service = service;
	}
	
	protected Topic createTopic(String topicName) throws JMSException {
		
		if (connection==null) {
			Object factory = service.createConnectionFactory(uri);
			ConnectionFactory connectionFactory = (ConnectionFactory)factory;		
			this.connection = connectionFactory.createConnection();
			connection.start();
		}
		if (session == null) {
		    this.session      = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		}
		
		return session.createTopic(topicName);
	}
	
	public void disconnect() throws EventException {
		try {
			if (connection!=null)        connection.close();
			if (session!=null)           session.close();
			
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

	public String getTerminateTopicName() {
		return terminateTopicName;
	}

	public void setTerminateTopicName(String terminateTopicName) {
		this.terminateTopicName = terminateTopicName;
	}
}
