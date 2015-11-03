package org.eclipse.scanning.event;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
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
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerImpl<U extends StatusBean> extends AbstractConnection implements IConsumer<U> {
	
	private static final Logger logger = LoggerFactory.getLogger(ConsumerImpl.class);
	private static final long ADAY = 24*60*60*1000; // ms

	private String                     name;
	private UUID                       consumerId;
	private IPublisher<U>              status;
	private IPublisher<HeartbeatBean>  alive;
	private ISubscriber<IBeanListener<TerminateBean>> killer;
	private ISubmitter<U>              mover;
	
	private Class<U>                   beanClass;

	private IProcessCreator<U>         runner;
	private boolean                    durable;
	private MessageConsumer            consumer;
	
	private volatile boolean           active;
	private volatile List<WeakReference<IConsumerProcess<U>>>  processes;

	@SuppressWarnings("unchecked")
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
		this.processes = new ArrayList<>(7);
		
		mover  = eservice.createSubmitter(uri, statusQName, service);
		status = eservice.createPublisher(uri, statusTName, service);
		status.setQueueName(statusQName); // We also update values in a queue.
		
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
		mover.disconnect();
		status.disconnect();
		alive.disconnect();
		killer.disconnect();
		try {
			if (connection!=null) connection.close();
		} catch (JMSException e) {
			throw new EventException("Cannot close consumer connection!", e);
		}
	}


	@Override
	public List<U> getSubmissionQueue() throws EventException {
		return getQueue(getSubmitQueueName());
	}

	@Override
	public List<U> getStatusQueue() throws EventException {
		return getQueue(getStatusQueueName());
	}

	private List<U> getQueue(String qName) throws EventException {
		QueueReader<U> reader = new QueueReader<U>(service);
		try {
			return reader.getBeans(uri, qName, getBeanClass());
		} catch (Exception e) {
			throw new EventException("Cannot get the beans for queue "+qName, e);
		}
	}

	@Override
	public void setRunner(IProcessCreator<U> runner) throws EventException {
		this.runner = runner;
		this.active = runner!=null;
	}
	
	@Override
	public void start() {
		
		final Thread consumerThread = new Thread("Consumer Thread "+getName()) {
			public void run() {
				try {
					ConsumerImpl.this.run();
				} catch (Exception ne) {
					logger.error("Internal error running consumer "+getName(), ne);
				}
			}
		};
		consumerThread.setDaemon(true);
		consumerThread.setPriority(Thread.NORM_PRIORITY-1);
		consumerThread.start();
	}
	
	@Override
	public void stop() throws EventException {
        setActive(false);
        final WeakReference<IConsumerProcess<U>>[] wra = processes.toArray(new WeakReference[processes.size()]);
        for (WeakReference<IConsumerProcess<U>> wr : wra) {
			if (wr.get()!=null) wr.get().terminate();
		}
	}
	
	@Override
	public void run() throws EventException {
		
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
                    final U bean   = service.unmarshal(str, beanClass);
	            	executeBean(bean);
	            	
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
	
	private void executeBean(U bean) throws EventException {
		
		// We record the bean in the status queue
		mover.submit(bean);
		
		// Run the process
		if (runner == null) {
			bean.setStatus(Status.FAILED);
			bean.setMessage("No runner set for consumer "+getName()+". Nothing run");
			status.broadcast(bean);
			throw new EventException("You must set the runner before executing beans from the queue!");
		}
		
		IConsumerProcess<U> process = runner.createProcess(bean, status);
		processes.add(new WeakReference<IConsumerProcess<U>>(process));
		process.execute(); // Depending on the process this may or may not run in a separate thread.
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
		
		QueueConnectionFactory connectionFactory = (QueueConnectionFactory)service.createConnectionFactory(uri);
		this.connection = connectionFactory.createQueueConnection();
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

	public Class<U> getBeanClass() {
		return beanClass;
	}

	public void setBeanClass(Class<U> beanClass) {
		this.beanClass = beanClass;
	}

	@Override
	public void clearStatusQueue() throws EventException{
		purgeQueue(getStatusQueueName());
	}

	private void purgeQueue(String qName) throws EventException {

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
				Message rem = consumer.receive(1000);	
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
}
