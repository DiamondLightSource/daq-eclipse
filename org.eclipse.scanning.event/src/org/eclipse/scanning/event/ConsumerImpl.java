package org.eclipse.scanning.event;

import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.alive.HeartbeatBean;
import org.eclipse.scanning.api.event.alive.TerminateBean;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerImpl<U> extends AbstractConnection implements IConsumer<U> {
	
	private static final Logger logger = LoggerFactory.getLogger(ConsumerImpl.class);
	private static final long ADAY = 24*60*60*1000; // ms

	private String                     name;
	private UUID                       consumerId;
	private IPublisher<U>              status;
	private IPublisher<HeartbeatBean>  alive;
	private ISubscriber<IBeanListener<TerminateBean>> killer;

	private IProcessCreator<U>         runner;
	private boolean                    active;
	private boolean                    durable;
	private MessageConsumer            consumer;

	ConsumerImpl(URI uri, String submitQName, 
			              String statusQName,
			              String statusTName, 
			              String heartbeatTName,
			              String terminateTName,
			              IEventConnectorService service,
			              IEventService          eservice) throws EventException {
		
		super(uri, submitQName, statusQName, statusTName, terminateTName, service);
		
		durable    = true;
		consumerId = UUID.randomUUID();
		name       = "Consumer "+consumerId; // This will hopefully be changed to something meaningful...
		
		status = eservice.createPublisher(uri, statusTName, service);
		alive  = eservice.createPublisher(uri, heartbeatTName,  service);
		killer = eservice.createSubscriber(uri, terminateTName, service);
		
		killer.addListener(new IBeanListener<TerminateBean>() {
			@Override
			public Class<TerminateBean> getBeanClass() {
				return TerminateBean.class;
			}

			@Override
			public void beanChangePerformed(BeanEvent<TerminateBean> evt) {
				TerminateBean tbean = evt.getBean();
				if (tbean.getConsumerId().equals(getConsumerId())) {
					try {
						disconnect();
					} catch (EventException e) {
						logger.error("An internal error occurred trying to terminate the consumer "+getName()+" "+getConsumerId());
					}
				}
			}
		});
	}

	@Override
	public void disconnect() throws EventException {
		setActive(false);
		status.disconnect();
		alive.disconnect();
		killer.disconnect();
		try {
			connection.close();
		} catch (JMSException e) {
			throw new EventException("Cannot close consumer connection!", e);
		}
	}


	@Override
	public List<U> getSubmissionQueue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<U> getStatusQueue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRunner(IProcessCreator<U> runner) throws EventException {
		this.runner = runner;
		this.active = runner!=null;
	}

	@Override
	public void start() throws EventException {
		
		if (runner!=null) {
			alive.setAlive(true);
		} else {
			throw new EventException("Cannot start a consumer without a runner to run things!");
		}
		
		long waitTime = 0;
		 
		while(isActive()){
        	try {
        		
        		// Consumes messages from the queue.
	        	Message m = getMessage(uri, getSubmitQueueName());
	            if (m!=null) {
		        	waitTime = 0; // We got a message
	            	
	            	// TODO FIXME Check if we have the max number of processes
	            	// exceeded and wait until we don't...
	            	
	            	TextMessage t = (TextMessage)m;
	            	
	            	final String     str  = t.getText();
	            	
	            	@SuppressWarnings("unchecked")
					Class<U> typeOfT = (Class<U>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	            	
	            	final U bean = service.unmarshal(str, typeOfT);
	            	status.broadcast(bean);
	            	
	            }
         		
        	} catch (Throwable ne) {
        		
        		if (ne.getClass().getSimpleName().endsWith("JsonMappingException")) {
            		logger.error("Fatal except deserializing object!", ne);
            		continue;
        		}
        		if (ne.getClass().getSimpleName().endsWith("UnrecognizedPropertyException")) {
        			logger.error("Cannot deserialize bean!", ne);
            		continue;
        		}
        		
        		if (!isDurable()) break;
        		        		
       			try {
					Thread.sleep(Constants.getNotificationFrequency());
				} catch (InterruptedException e) {
					throw new EventException("The consumer was unable to wait!", e);
				}
       			
       			waitTime+=Constants.getNotificationFrequency();
    			checkTime(waitTime); 
    			
        		logger.warn(getName()+" ActiveMQ connection to "+uri+" lost.");
        		logger.warn("We will check every 2 seconds for 24 hours, until it comes back.");

        		continue;
        	}

		}
	}
	
	protected void checkTime(long waitTime) {
		
		if (waitTime>ADAY) {
			setActive(false);
			logger.warn("ActiveMQ permanently lost. "+getName()+" will now shutdown!");
			System.exit(0);
		}
	}

	private Message getMessage(URI uri, String submitQName) throws InterruptedException, JMSException {
		
		try {
			if (this.consumer == null) {
				this.consumer = createConsumer(uri, submitQName);
			}
			
			return consumer.receive(1000);
			
		} catch (Exception ne) {
			consumer = null;
			try {
				connection.close();
			} catch (Exception expected) {
				logger.info("Cannot close old connection", ne);
			}
			throw ne;
		}
	}

	private MessageConsumer createConsumer(URI uri, String submitQName) throws JMSException {
		
		ConnectionFactory connectionFactory = (ConnectionFactory)service.createConnectionFactory(uri);
		this.connection = connectionFactory.createConnection();
		Session   session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Queue queue = session.createQueue(submitQName);

		final MessageConsumer consumer = session.createConsumer(queue);
		connection.start();
		
		logger.warn(getName()+" Submission ActiveMQ connection to "+uri+" made.");
		
		return consumer;
	}


	@Override
	public IProcessCreator<U> getRunner() {
		return runner;
	}

	@Override
	public UUID getConsumerId() {
		return consumerId;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isDurable() {
		return durable;
	}

	public void setDurable(boolean durable) {
		this.durable = durable;
	}

}
