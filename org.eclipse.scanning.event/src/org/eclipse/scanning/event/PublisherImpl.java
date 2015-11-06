package org.eclipse.scanning.event;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URI;
import java.util.Enumeration;
import java.util.UUID;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.alive.ConsumerStatus;
import org.eclipse.scanning.api.event.alive.HeartbeatBean;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PublisherImpl<T> extends AbstractConnection implements IPublisher<T> {
	
	private static final Logger logger = LoggerFactory.getLogger(PublisherImpl.class);
	
	// JMS things, these are null when not running and 
	// are cleaned up at the end of a run.
	private MessageProducer scanProducer, heartbeatProducer;
	private boolean         alive;
	private String          queueName;
	
	private String          consumerName;
	private UUID            consumerId;

	private PrintStream     out;

	public PublisherImpl(URI uri, String topic, IEventConnectorService service) {
		super(uri, topic, service);
	}

	@Override
	public void broadcast(T bean) throws EventException {
		
		try {
		    if (getTopicName()!=null) if (scanProducer==null) scanProducer = createProducer(getTopicName());
			if (queueName!=null) updateSet(bean);
			if (getTopicName()!=null) send(scanProducer, bean, 1000);

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
		if (out!=null) out.println(json);
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
					beat.setConceptionTime(System.currentTimeMillis());
					
					// Here we are sending the message out to the topic
					while(isAlive()) {
						try {
							
							// Sleep for a bit
							Thread.sleep(Constants.getNotificationFrequency());		

			                // The producer might need to be reconnected.
							if (heartbeatProducer==null) heartbeatProducer = createProducer(getTopicName());
							beat.setPublishTime(System.currentTimeMillis());
							beat.setConsumerId(consumerId);
							beat.setConsumerName(consumerName);
							beat.setConsumerStatus(ConsumerStatus.ALIVE);
							beat.setBeamline(System.getenv("BEAMLINE"));
							beat.setHostName(InetAddress.getLocalHost().getHostName());

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

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	/**
	 * 
	 * @param bean
	 * @throws Exception 
	 */
	private boolean updateSet(T bean) throws Exception {
		
		
		Queue     queue = createQueue(getQueueName());
		QueueBrowser qb = qSession.createBrowser(queue);

		@SuppressWarnings("rawtypes")
		Enumeration  e  = qb.getEnumeration();

		String jMSMessageID = null;
		while(e.hasMoreElements()) {
			Message m = (Message)e.nextElement();
			if (m==null) continue;
			if (m instanceof TextMessage) {
				TextMessage t = (TextMessage)m;

				final T qbean = service.unmarshal(t.getText(), (Class<T>)bean.getClass());
				if (qbean==null) continue;
				if (isSame(qbean, bean)) {
					jMSMessageID = t.getJMSMessageID();
					break;
				}
			}
		}

		qb.close();

		if (jMSMessageID!=null) {
			MessageConsumer consumer = session.createConsumer(queue, "JMSMessageID = '"+jMSMessageID+"'");
			Message m = consumer.receive(500);
			if (m!=null && m instanceof TextMessage) {
				MessageProducer producer = session.createProducer(queue);
				
				TextMessage t = session.createTextMessage(service.marshal(bean));
				t.setJMSMessageID(m.getJMSMessageID());
				t.setJMSExpiration(m.getJMSExpiration());
				t.setJMSTimestamp(m.getJMSTimestamp());
				t.setJMSPriority(m.getJMSPriority());
				t.setJMSCorrelationID(m.getJMSCorrelationID());
	
				producer.send(t);
				
				producer.close();
				
				return true;
			}
		}

		return false;
	}

	public String getConsumerName() {
		return consumerName;
	}

	public void setConsumerName(String consumerName) {
		this.consumerName = consumerName;
	}

	public UUID getConsumerId() {
		return consumerId;
	}

	public void setConsumerId(UUID consumerId) {
		this.consumerId = consumerId;
	}

	@Override
	public void setLoggingStream(PrintStream stream) {
		this.out = stream;
		if (out!=null) {
			out.println("Publisher for consumer name "+getConsumerName());
		}
	}

}
