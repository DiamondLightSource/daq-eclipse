package org.eclipse.scanning.event;

import java.net.URI;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.alive.HeartbeatBean;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PublisherImpl<T> extends AbstractConnection implements IPublisher<T> {
	
	private static final Logger logger = LoggerFactory.getLogger(PublisherImpl.class);
	
	// JMS things, these are null when not running and 
	// are cleaned up at the end of a run.
	private MessageProducer scanProducer, heartbeatProducer;
	private boolean         alive;

	public PublisherImpl(URI uri, String topic, IEventConnectorService service) {
		super(uri, topic, service);
	}

	@Override
	public void broadcast(T bean) throws EventException {
		
		try {
		    if (scanProducer==null) scanProducer = createProducer(getTopicName());
		    send(scanProducer, bean, 1000);

		} catch (JMSException ne) {
			throw new EventException("Unable to start the scan producer using uri "+uri+" and topic "+getTopicName(), ne);
			
		} catch (Exception neOther) {
			throw new EventException("Unable to prepare and send the event "+bean, neOther);
		}
	}
	
	protected void send(MessageProducer producer, Object message, long messageLifetime)  throws Exception {

		String json = service.marshal(message);
		TextMessage temp = session.createTextMessage(json);
		producer.send(temp, DeliveryMode.NON_PERSISTENT, 1, messageLifetime);			
	}

	public boolean isAlive() {
		return alive;
	}

	@Override
	public void setAlive(boolean alive) throws EventException {
		
		this.alive = alive;
		
		if (alive) {
			try {
				if (heartbeatProducer==null) heartbeatProducer = createProducer(getTopicName());
			} catch (JMSException ne) {
				throw new EventException("Unable to start the heartbeat producer using uri "+uri+" and topic "+getTopicName());
			}
			
			Thread aliveThread = new Thread(new Runnable() {
				public void run() {

					long waitTime = 0;
					
					HeartbeatBean beat = new HeartbeatBean();
					beat.setBeamline(System.getenv("BEAMLINE"));
					beat.setConceptionTime(System.currentTimeMillis());
					
					// Here we are sending the message out to the topic
					while(isAlive()) {
						try {
							
							// Sleep for a bit
							Thread.sleep(Constants.getNotificationFrequency());		

			                // The producer might need to be reconnected.
							if (heartbeatProducer==null) heartbeatProducer = createProducer(getTopicName());
							beat.setPublishTime(System.currentTimeMillis());
							send(heartbeatProducer, beat, 5000);
							waitTime = 0; // We sent something
							
							

						} catch (Exception ne) {
								
							heartbeatProducer = null;
							connection = null;
							session    = null;
							
							waitTime+=Constants.getNotificationFrequency();
							if (waitTime>Constants.getTimeout()) {
								logger.error("Connection to URI "+uri+" is non-viable, no hearbeats will be sent.");
							    PublisherImpl.this.alive = false;
								return;
							}
							
			        		logger.warn("Event publisher heartbeat connection to "+uri+" lost.");
			        		logger.warn("We will check every 2 seconds for 24 hours, until it comes back.");
			        		
							continue;							
						} 
					}
				}
			});
			aliveThread.setName("Alive Notification "+getTopicName()+" ");
			aliveThread.setDaemon(true);
			aliveThread.setPriority(Thread.MIN_PRIORITY);
			aliveThread.start();
			
		}

	}

	private MessageProducer createProducer(String topicName) throws JMSException {
		final Topic topic = createTopic(topicName);
		return session.createProducer(topic);
	}

	@Override
	public void disconnect() throws EventException {
		try {
			alive = false;
			if (scanProducer!=null)      scanProducer.close();
			if (heartbeatProducer!=null) heartbeatProducer.close();
			
			super.disconnect();
			
		} catch (JMSException ne) {
			throw new EventException("Internal error - unable to close connection!", ne);
		
		} finally {
			scanProducer = null;
			heartbeatProducer = null;
		}
	}

}
