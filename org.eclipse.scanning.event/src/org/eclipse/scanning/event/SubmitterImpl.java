package org.eclipse.scanning.event;

import java.net.URI;
import java.util.Enumeration;
import java.util.UUID;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
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

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.osgi.framework.FrameworkUtil;

class SubmitterImpl<T extends StatusBean> extends AbstractQueueConnection<T> implements ISubmitter<T> {

	// Message things
	private String uniqueId;
	private int    priority;
	private long   lifeTime;
	private long   timestamp;

	SubmitterImpl(URI uri, String submitQueue, IEventConnectorService service) {
		super(uri, null, service);
		setSubmitQueueName(submitQueue);
	}

	@Override
	public void submit(T bean) throws EventException {
        submit(bean, true);
	}
	
	@Override
	public void submit(T bean, boolean prepareBean) throws EventException {
		      
		Connection      send     = null;
		Session         session  = null;
		MessageProducer producer = null;

		try {

			QueueConnectionFactory connectionFactory = (QueueConnectionFactory)service.createConnectionFactory(uri);
			send              = connectionFactory.createConnection();

			session = send.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Queue queue = session.createQueue(getSubmitQueueName());

			producer = session.createProducer(queue);
			producer.setDeliveryMode(DeliveryMode.PERSISTENT);

			if (bean.getSubmissionTime()<1) bean.setSubmissionTime(System.currentTimeMillis());
			if (getPriority()<1)  setPriority(1);
			if (getLifeTime()<1)  setLifeTime(7*24*60*60*1000); // 7 days in ms

			if (uniqueId==null) {
				uniqueId = bean.getUniqueId()!=null ? bean.getUniqueId() : UUID.randomUUID().toString();
			}
			if (prepareBean) {
				if (bean.getUserName()==null) bean.setUserName(System.getProperty("user.name"));
				if (bean.getUniqueId()==null) bean.setUniqueId(uniqueId);
				if (getTimestamp()>0) bean.setSubmissionTime(getTimestamp());
			}

			String json = null;
			try {
				if (bean.getBundle()==null) {
					try {
						// In OSGi mode, this sets the bundle.
						// In non-OSGi mode this takes a while to fail.
					    bean.setBundle(FrameworkUtil.getBundle(bean.getClass()).getSymbolicName());
					} catch (Exception ne) {
						bean.setBundle(null);
					}
				}
				if (bean.getBeanClass()==null) bean.setBeanClass(bean.getClass().getName());
				json = service.marshal(bean);
			} catch (Exception e) {
				throw new EventException("Unable to marshall bean "+bean, e);
			}

			TextMessage message = session.createTextMessage(json);

			message.setJMSMessageID(bean.getUniqueId());
			message.setJMSExpiration(getLifeTime());
			message.setJMSTimestamp(getTimestamp());
			message.setJMSPriority(getPriority());

			producer.send(message);


		} catch (Exception e) {
			throw new EventException("Problem opening connection to queue! ", e);

		} finally {
			try {
				if (send!=null)     send.close();
				if (session!=null)  session.close();
				if (producer!=null) producer.close();
			} catch (Exception e) {
				throw new EventException("Cannot close connection as expected!", e);
			}
		}

	}
	
	@Override
	public boolean remove(T bean) throws EventException {
			
		QueueConnection send     = null;
		QueueSession    session  = null;
		MessageProducer producer = null;

		try {

			QueueConnectionFactory connectionFactory = (QueueConnectionFactory)service.createConnectionFactory(uri);
			send              = connectionFactory.createQueueConnection();

			session = send.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			Queue queue = session.createQueue(getSubmitQueueName());
			QueueBrowser qb = session.createBrowser(queue);
	
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
				Message m = consumer.receive(1000);
				return m!=null;
			}
	
			return false;
			
		} catch (Exception ne) {
			throw new EventException("Cannot remove item "+bean, ne);
		}  finally {
			try {
				if (send!=null)     send.close();
				if (session!=null)  session.close();
				if (producer!=null) producer.close();
			} catch (Exception e) {
				throw new EventException("Cannot close connection as expected!", e);
			}
		}

	}


	public String getUniqueId() {
		return uniqueId;
	}


	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}


	public int getPriority() {
		return priority;
	}


	public void setPriority(int priority) {
		this.priority = priority;
	}


	public long getLifeTime() {
		return lifeTime;
	}


	public void setLifeTime(long lifeTime) {
		this.lifeTime = lifeTime;
	}


	public long getTimestamp() {
		return timestamp;
	}


	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}
