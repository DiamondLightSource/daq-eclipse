package org.eclipse.scanning.event;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

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

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.core.IQueueConnection;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;

public abstract class AbstractQueueConnection<U extends StatusBean> extends AbstractConnection implements IQueueConnection<U>{

	
	
	private Class<U>                      beanClass;

	AbstractQueueConnection(URI uri, String topic, IEventConnectorService service) {
		super(uri, topic, service);
	}

	AbstractQueueConnection(URI uri, String submitQName, String statusQName, String statusTName, String terminateTName, IEventConnectorService service) {
        super(uri, submitQName, statusQName, statusTName, terminateTName, service);
	}
	

	@Override
	public Class<U> getBeanClass() {
		return (Class<U>)beanClass;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class<U> getBeanClass(String json) throws ClassNotFoundException {
		return getBeanClass() !=null
		       ? getBeanClass()
			   : (Class)EventServiceImpl.getClassFromJson(json);
	}
	

	@Override
	public void setBeanClass(Class<U> beanClass)  throws EventException{
		this.beanClass = beanClass;
	}


	@Override
	public List<U> getQueue(String qName, String fieldName) throws EventException {
		
		Comparator<U> c = null;
		
		if (fieldName!=null) c = getComparator(fieldName);
			

		QueueReader<U> reader = new QueueReader<U>(service, c);
		try {
			return reader.getBeans(uri, qName, getBeanClass());
		} catch (Exception e) {
			throw new EventException("Cannot get the beans for queue "+qName+" of type "+getBeanClass(), e);
		}
	}
	

	private Comparator<U> getComparator(final String fieldName) {
		
		if ("submissionTime".equals(fieldName)) { // Short cu no reflection
			
			return new Comparator<U>() {		
				@Override
				public int compare(U o1, U o2) {
					// Newest first!
			        long t1 = o2.getSubmissionTime();
			        long t2 = o1.getSubmissionTime();
			        if (t1<t2) return -1;
			        if (t1==t2) return o1.equals(o2) ? 0 : 1;
			        return 1;
				}
			};
			
		} else {
			
			return new Comparator<U>() {		
				@Override
				public int compare(U o1, U o2) {	
					try {
						Object val1 = o1.getClass().getMethod(getGetterName(fieldName)).invoke(o1);
						Object val2 = o2.getClass().getMethod(getGetterName(fieldName)).invoke(o2);
						
						if (val1 instanceof Number && val2 instanceof Number) {
							Number n1 = (Number)val1;
							Number n2 = (Number)val2;
					        double t1 = n1.doubleValue();
					        double t2 = n2.doubleValue();
					        if (t1<t2) return -1;
					        if (t1==t2) return o1.equals(o2) ? 0 : 1;
					        return 1;
						}
						
						return val1.toString().compareTo(val2.toString());
						
					} catch (Exception ne) {
						return o1.toString().compareTo(o2.toString());
					}
				}
			};

		}
	}
	private static String getGetterName(final String fieldName) {
		if (fieldName == null)
			return null;
		return getName("get", fieldName);
	}
	private static String getName(final String prefix, final String fieldName) {
		return prefix + getFieldWithUpperCaseFirstLetter(fieldName);
	}
	public static String getFieldWithUpperCaseFirstLetter(final String fieldName) {
		return fieldName.substring(0, 1).toUpperCase(Locale.US) + fieldName.substring(1);
	}



	protected Map<String, U> getMap(String queueName) throws EventException {
		
		final List<U> queue = getQueue(queueName, null);
		if (queue==null || queue.isEmpty()) return null;
		final HashMap<String, U> id = new HashMap<>(queue.size());
		for (U u : queue) id.put(u.getUniqueId(), u);
		return id;
	}

	@Override
	public void cleanQueue(String queueName) throws EventException {

		try {
			QueueConnection qCon = null;
	
			try {
				QueueConnectionFactory connectionFactory = (QueueConnectionFactory)service.createConnectionFactory(uri);
				qCon  = connectionFactory.createQueueConnection(); 
				QueueSession    qSes  = qCon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
				Queue queue   = qSes.createQueue(queueName);
				qCon.start();
	
				QueueBrowser qb = qSes.createBrowser(queue);
	
				@SuppressWarnings("rawtypes")
				Enumeration  e  = qb.getEnumeration();
	
				Map<String, StatusBean> failIds = new LinkedHashMap<String, StatusBean>(7);
				List<String>          removeIds = new ArrayList<String>(7);
				while(e.hasMoreElements()) {
					Message m = (Message)e.nextElement();
					if (m==null) continue;
					if (m instanceof TextMessage) {
						TextMessage t = (TextMessage)m;
	
						try {
							final String     json  = t.getText();
							final StatusBean qbean = service.unmarshal(json, getBeanClass(json));
							if (qbean==null)               continue;
							if (qbean.getStatus()==null)   continue;
							if (!qbean.getStatus().isStarted()) {
								failIds.put(t.getJMSMessageID(), qbean);
								continue;
							}
	
							// If it has failed, we clear it up
							if (qbean.getStatus()==Status.FAILED) {
								removeIds.add(t.getJMSMessageID());
								continue;
							}
							if (qbean.getStatus()==Status.NONE) {
								removeIds.add(t.getJMSMessageID());
								continue;
							}
	
							// If it is running and older than a certain time, we clear it up
							if (qbean.getStatus()==Status.RUNNING) {
								final long submitted = qbean.getSubmissionTime();
								final long current   = System.currentTimeMillis();
								if (current-submitted > getMaximumRunningAge()) {
									removeIds.add(t.getJMSMessageID());
									continue;
								}
							}
	
							if (qbean.getStatus().isFinal()) {
								final long submitted = qbean.getSubmissionTime();
								final long current   = System.currentTimeMillis();
								if (current-submitted > getMaximumCompleteAge()) {
									removeIds.add(t.getJMSMessageID());
								}
							}
	
						} catch (Exception ne) {
							logger.warn("Message "+t.getText()+" is not legal and will be removed.", ne);
							removeIds.add(t.getJMSMessageID());
						}
					}
				}
	
				// We fail the non-started jobs now - otherwise we could
				// actually start them late. TODO check this
				final List<String> ids = new ArrayList<String>();
				ids.addAll(failIds.keySet());
				ids.addAll(removeIds);
	
				if (ids.size()>0) {
	
					for (String jMSMessageID : ids) {
						MessageConsumer consumer = qSes.createConsumer(queue, "JMSMessageID = '"+jMSMessageID+"'");
						Message m = consumer.receive(1000);
						if (removeIds.contains(jMSMessageID)) continue; // We are done
	
						if (m!=null && m instanceof TextMessage) {
							MessageProducer producer = qSes.createProducer(queue);
							final StatusBean    bean = failIds.get(jMSMessageID);
							bean.setStatus(Status.FAILED);
							producer.send(qSes.createTextMessage(service.marshal(bean)));
	
							logger.warn("Failed job "+bean.getName()+" messageid("+jMSMessageID+")");
	
						}
					}
				}
			} finally {
				if (qCon!=null) qCon.close();
			}
		} catch (Exception ne) {
			throw new EventException("Problem connecting to "+queueName+" in order to clean it!", ne);
		}
	}


	@Override
	public void clearQueue(String qName) throws EventException {

		QueueConnection qCon = null;
		try {
			QueueConnectionFactory connectionFactory = (QueueConnectionFactory)service.createConnectionFactory(uri);
			qCon  = connectionFactory.createQueueConnection(); // This times out when the server is not there.
			QueueSession    qSes  = qCon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			Queue queue   = qSes.createQueue(qName);
			qCon.start();

			QueueBrowser qb = qSes.createBrowser(queue);

			@SuppressWarnings("rawtypes")
			Enumeration  e  = qb.getEnumeration();					
			while(e.hasMoreElements()) {
				Message msg = (Message)e.nextElement();
				MessageConsumer consumer = qSes.createConsumer(queue, "JMSMessageID = '"+msg.getJMSMessageID()+"'");
				Message rem = consumer.receive(500);	
				if (rem!=null) System.out.println("Removed "+rem);
				consumer.close();
			}

		} catch (Exception ne) {
			throw new EventException(ne);

		} finally {
			if (qCon!=null) {
				try {
					qCon.close();
				} catch (JMSException e) {
					logger.error("Cannot close queue!", e);
				}
			}
		}
	}
	
	protected static final long TWO_DAYS = 48*60*60*1000; // ms
	protected static final long A_WEEK   = 7*24*60*60*1000; // ms

	/**
	 * Defines the time in ms that a job may be in the running state
	 * before the consumer might consider it for deletion. If a consumer
	 * is restarted it will normally delete old running jobs older than 
	 * this age.
	 * 
	 * @return
	 */
	public long getMaximumRunningAge() {
		if (System.getProperty("org.eclipse.scanning.event.consumer.maximumRunningAge")!=null) {
			return Long.parseLong(System.getProperty("org.eclipse.scanning.event.consumer.maximumRunningAge"));
		}
		return TWO_DAYS;
	}
		
	/**
	 * Defines the time in ms that a job may be in the complete (or other final) state
	 * before the consumer might consider it for deletion. If a consumer
	 * is restarted it will normally delete old complete jobs older than 
	 * this age.
	 * 
	 * @return
	 */
	public long getMaximumCompleteAge() {
		if (System.getProperty("org.eclipse.scanning.event.consumer.maximumCompleteAge")!=null) {
			return Long.parseLong(System.getProperty("org.eclipse.scanning.event.consumer.maximumCompleteAge"));
		}
		return A_WEEK;
	}

}
