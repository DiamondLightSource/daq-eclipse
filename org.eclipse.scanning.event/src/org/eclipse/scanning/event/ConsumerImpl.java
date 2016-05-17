package org.eclipse.scanning.event;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.alive.ConsumerCommandBean;
import org.eclipse.scanning.api.event.alive.HeartbeatBean;
import org.eclipse.scanning.api.event.alive.KillBean;
import org.eclipse.scanning.api.event.alive.PauseBean;
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

public class ConsumerImpl<U extends StatusBean> extends AbstractQueueConnection<U> implements IConsumer<U> {
	
	private static final Logger logger = LoggerFactory.getLogger(ConsumerImpl.class);
	private static final long   ADAY   = 24*60*60*1000; // ms

	private String                        name;
	private UUID                          consumerId;
	private IPublisher<U>                 status;
	private IPublisher<HeartbeatBean>     alive;
	private ISubscriber<IBeanListener<U>> manager;
	private ISubscriber<IBeanListener<ConsumerCommandBean>> command;
	private ISubmitter<U>                 mover;

	private IProcessCreator<U>            runner;
	private boolean                       durable;
	private MessageConsumer               consumer;
	
	private volatile boolean              active;
	private volatile Map<String, WeakReference<IConsumerProcess<U>>>  processes;
	private Map<String, U>                overrideMap;
	
	/*
	 * Concurrency design recommended by Keith Ralphs after investigating
	 * how to pause and resume a collection cycle using Reentrant locks.
	 * Design requires these three fields.
	 */
	private ReentrantLock    lock;
	private Condition        paused;
	private volatile boolean awaitPaused;


	ConsumerImpl(URI uri, String submitQName, 
			              String statusQName,
			              String statusTName, 
			              String heartbeatTName,
			              String commandTName,
			              IEventConnectorService service,
			              IEventService          eservice) throws EventException {
		
		super(uri, submitQName, statusQName, statusTName, commandTName, service, eservice);
		this.lock      = new ReentrantLock();
		this.paused    = lock.newCondition();
		
		durable    = true;
		consumerId = UUID.randomUUID();
		name       = "Consumer "+consumerId; // This will hopefully be changed to something meaningful...
		this.processes       = new Hashtable<>(7); // Synch!
		
		mover  = eservice.createSubmitter(uri, statusQName);
		status = eservice.createPublisher(uri, statusTName);
		status.setQueueName(statusQName); // We also update values in a queue.
		
		if (heartbeatTName!=null) { 
			alive  = eservice.createPublisher(uri, heartbeatTName);
			alive.setConsumerId(consumerId);
			alive.setConsumerName(getName());
		}
				
		if (commandTName!=null) {
			command = eservice.createSubscriber(uri, commandTName);
			command.addListener(new CommandListener());
		}
	}
	
	protected class CommandListener implements IBeanListener<ConsumerCommandBean> {
		@Override
		public void beanChangePerformed(BeanEvent<ConsumerCommandBean> evt) {
			ConsumerCommandBean bean = evt.getBean();
			if (isCommandForMe(bean)) {
				if (bean instanceof KillBean)   terminate((KillBean)bean);
				if (bean instanceof PauseBean)  processPause((PauseBean)bean);
			}
		}
	}
	
	protected void processPause(PauseBean bean) {
		try {
			if (bean.isPause()) {
				pause();
			} else {
				resume();
			}
		} catch (Exception ne) {
			logger.error("Unable to process pause command on consumer '"+getName()+"'. Consumer will stop.", ne);
			try {
				stop();
				disconnect();
			} catch (EventException e) {
				logger.error("An internal error occurred trying to terminate the consumer "+getName()+" "+getConsumerId());
			}
		}
	}

	protected void terminate(KillBean kbean) {
		try {
			stop();
			if (kbean.isDisconnect()) disconnect();
		} catch (EventException e) {
			logger.error("An internal error occurred trying to terminate the consumer "+getName()+" "+getConsumerId());
		}
		if (kbean.isExitProcess()) {
			try {
				Thread.sleep(2500);
			} catch (InterruptedException e) {
				logger.error("Unable to pause before exit", e);
			}
			System.exit(0); // Normal orderly exit
		}
	}

	protected boolean isCommandForMe(ConsumerCommandBean bean) {
		if (bean.getConsumerId()!=null) { 
			if (bean.getConsumerId().equals(getConsumerId())) return true;
		}
		if (bean.getQueueName()!=null) {
			if (bean.getQueueName().equals(getSubmitQueueName())) return true;
		}
		return false;
	}

	protected void updateQueue(U bean) throws EventException {
		
		Session session = null;
		try {
			pause();
			
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Queue queue = session.createQueue(getSubmitQueueName());
			QueueBrowser qb = session.createBrowser(queue);
	
			@SuppressWarnings("rawtypes")
			Enumeration  e  = qb.getEnumeration();					
			while(e.hasMoreElements()) {
				
				Message msg = (Message)e.nextElement();
				TextMessage t = (TextMessage)msg;
				String json   = t.getText();
				final StatusBean b = service.unmarshal(json, StatusBean.class);
				
				MessageConsumer consumer = session.createConsumer(queue, "JMSMessageID = '"+msg.getJMSMessageID()+"'");
				Message rem = consumer.receive(500);	
				consumer.close();
				
				if(rem == null && b.getUniqueId().equals(bean.getUniqueId())) { // Something went wrong, not sure why it does this
					if (overrideMap == null) overrideMap = new Hashtable<>(7);
					overrideMap.put(b.getUniqueId(), bean);
					continue;
				}
				
				MessageProducer producer = session.createProducer(queue);
				if (b.getUniqueId().equals(bean.getUniqueId())) {
					
					b.setStatus(bean.getStatus());
					t = session.createTextMessage(service.marshal(b));
					t.setJMSMessageID(rem.getJMSMessageID());
					t.setJMSExpiration(rem.getJMSExpiration());
					t.setJMSTimestamp(rem.getJMSTimestamp());
					t.setJMSPriority(rem.getJMSPriority());
					t.setJMSCorrelationID(rem.getJMSCorrelationID());
				}
				producer.send(t);
				producer.close();
			
			}
						
		} catch (Exception ne) {
			throw new EventException("Cannot reorder queue!", ne);
			
		} finally {
			resume();
			try {
				if (session!=null) session.close();
			} catch (JMSException e) {
				throw new EventException("Cannot close session!", e);
			}
		}
	}

	@Override
	public void disconnect() throws EventException {
		
		if (isActive()) stop();
		
		super.disconnect();
		setActive(false);
		mover.disconnect();
		status.disconnect();
		alive.disconnect();
		command.disconnect();
		if (overrideMap!=null) overrideMap.clear();
		try {
			if (connection!=null) connection.close();
		} catch (JMSException e) {
			throw new EventException("Cannot close consumer connection!", e);
		}
	}


	@Override
	public List<U> getSubmissionQueue() throws EventException {
		return getQueue(getSubmitQueueName(), null);
	}

	@Override
	public List<U> getStatusSet() throws EventException {
		return getQueue(getStatusSetName(), "submissionTime");
	}

	@Override
	public void setRunner(IProcessCreator<U> runner) throws EventException {
		this.runner = runner;
		this.active = runner!=null;
	}
	
	@Override
	public void start() throws EventException {
				
		final Thread consumerThread = new Thread("Consumer Thread "+getName()) {
			public void run() {
				try {
					ConsumerImpl.this.run();
				} catch (Exception ne) {
					logger.error("Internal error running consumer "+getName(), ne);
					ne.printStackTrace();
					try {
						ConsumerImpl.this.stop();
					} catch (EventException e) {
						logger.error("Cannot complete stop", ne);
						ne.printStackTrace();
					}
				}
			}
		};
		setActive(true);
		consumerThread.setDaemon(true);
		consumerThread.setPriority(Thread.NORM_PRIORITY-1);
		consumerThread.start();
	}
	

	private void startJobManager() throws EventException {
		
		if (manager!=null) manager.disconnect();
		manager = eservice.createSubscriber(uri, getStatusTopicName());
		manager.addListener(new TerminateListener());
	}
	
	protected class TerminateListener implements IBeanListener<U> {
		@Override
		public void beanChangePerformed(BeanEvent<U> evt) {
			U bean = evt.getBean();
			if (!bean.getStatus().isRequest()) return;
			
			WeakReference<IConsumerProcess<U>> ref = processes.get(bean.getUniqueId());
			try {
				if (ref==null) { // Might be in submit queue still
					updateQueue(bean);

				} else {
					IConsumerProcess<U> process = ref.get();
					if (process!=null) {
						process.getBean().setStatus(bean.getStatus());
						process.getBean().setMessage(bean.getMessage());
						if (bean.getStatus()==Status.REQUEST_TERMINATE) {
							processes.remove(bean.getUniqueId());
							process.terminate();
						} else if (bean.getStatus()==Status.REQUEST_PAUSE) {
							process.pause();
						} else if (bean.getStatus()==Status.REQUEST_RESUME) {
							process.resume();
						}
					}
				}
			} catch (EventException ne) {
				logger.error("Internal error, please contact your support representative.", ne);
			}
		}
	}

	@Override
	public void stop() throws EventException {
        alive.setAlive(false); // Broadcasts that we are being killed
        setActive(false);
        @SuppressWarnings("unchecked")
		final WeakReference<IConsumerProcess<U>>[] wra = processes.values().toArray(new WeakReference[processes.size()]);
        for (WeakReference<IConsumerProcess<U>> wr : wra) {
			if (wr.get()!=null) wr.get().terminate();
		}
        processes.clear();
	}

	@Override
	public void run() throws EventException {
		
		startJobManager();

		if (runner!=null) {
			alive.setAlive(true);
		} else {
			throw new EventException("Cannot start a consumer without a runner to run things!");
		}
		
		long waitTime = 0;
		 
		while(isActive()) {
        	try {
        		checkPaused(); // blocks until not paused.
        		
        		// Consumes messages from the queue.
	        	Message m = getMessage(uri, getSubmitQueueName());
	            if (m!=null) {
		        	waitTime = 0; // We got a message
	            	
	            	// TODO FIXME Check if we have the max number of processes
	            	// exceeded and wait until we don't...
	            	
	            	TextMessage t = (TextMessage)m;
	            	
	            	final String json  = t.getText();
	            	
					@SuppressWarnings("unchecked")
					final U bean   = (U) service.unmarshal(json, StatusBean.class);
                    
	            	executeBean(bean);
	            	
	            }
	            
        	} catch (EventException | InterruptedException ne) {
        		ne.printStackTrace();
				logger.error("Cannot consume message ", ne);
       		    if (isDurable()) continue;
        		break;
         		
        	} catch (Throwable ne) {
        		
        		if (ne.getClass().getSimpleName().contains("Json")) {
            		logger.error("Fatal except deserializing object!", ne);
            		continue;
        		}
        		if (ne.getClass().getSimpleName().endsWith("UnrecognizedPropertyException")) {
        			logger.error("Cannot deserialize bean!", ne);
            		continue;
        		}
        		
        		if (ne.getClass().getSimpleName().endsWith("ClassCastException")) {
            		ne.printStackTrace();
    				logger.error("Problem with serialization?", ne);
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
	
	private void checkPaused() throws Exception {
		
		if (!isActive()) throw new Exception("The consumer is not active and cannot be paused!");

		// Check the locking using a condition
    	if(!lock.tryLock(1, TimeUnit.SECONDS)) {
    		throw new EventException("Internal Error - Could not obtain lock to run device!");    		
    	}
    	try {
    		if (!isActive()) throw new Exception("The consumer is not active and cannot be paused!");
       	    if (awaitPaused) {
       	    	setActive(false);
        		paused.await(); // Until unpaused
       	    	setActive(true);
       	    }
    	} finally {
    		lock.unlock();
    	}
		
	}
	
	private void pause() throws EventException {
		
		if (!isActive()) throw new EventException("The consumer is not active and cannot be paused!");
		try {
			lock.lockInterruptibly();
		} catch (Exception ne) {
			throw new EventException(ne);
		}
		
		try {
			awaitPaused = true;
			if (consumer!=null) consumer.close();
			consumer = null; // Force unpaused consumers to make a new connection.
			logger.info(getName()+" is Paused");
			
		} catch (Exception ne) {
			throw new EventException(ne);
		} finally {
			lock.unlock();
		}
	}

	public void resume() throws EventException {
		
		try {
			lock.lockInterruptibly();
		} catch (Exception ne) {
			throw new EventException(ne);
		}
		
		try {
			awaitPaused = false;
			// We don't have to actually start anything again because the getMessage(...) call reconnects automatically.
			paused.signalAll();
			logger.info(getName()+" is Resumed");
			
		} finally {
			lock.unlock();
		}
	}

	private void executeBean(U bean) throws EventException, InterruptedException {
		
		// We record the bean in the status queue
		if (overrideMap!=null && overrideMap.containsKey(bean.getUniqueId())) {
			U o = overrideMap.remove(bean.getUniqueId());
			bean.setStatus(o.getStatus());
		}
		logger.trace("Moving "+bean+" to "+mover.getSubmitQueueName());
		mover.submit(bean);
		
		// Run the process
		if (runner == null) {
			bean.setStatus(Status.FAILED);
			bean.setMessage("No runner set for consumer "+getName()+". Nothing run");
			status.broadcast(bean);
			throw new EventException("You must set the runner before executing beans from the queue!");
		}
		
		if (processes.containsKey(bean.getUniqueId())) {
			throw new EventException("The bean with unique id '"+bean.getUniqueId()+"' has already been used. Cannot run the same uuid twice!");
		}
		
		// We peal off the most recent bean from the submission queue
		
		if (bean.getStatus()==Status.REQUEST_TERMINATE) {
			bean.setStatus(Status.TERMINATED);
			bean.setMessage("Run aborted before started");
			status.broadcast(bean);
			return;
		}
		
		if (bean.getStatus().isFinal()) return; // This is not the bean you are looking for.

		IConsumerProcess<U> process = runner.createProcess(bean, status);
		processes.put(bean.getUniqueId(), new WeakReference<IConsumerProcess<U>>(process));
		
		process.start(); // Depending on the process may run in a separate thread (default is not to)
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
			return consumer.receive(500);
			
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
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Queue queue = session.createQueue(submitQName);

		final MessageConsumer consumer = session.createConsumer(queue);
		connection.start();
		
		logger.info(getName()+" Submission ActiveMQ connection to "+uri+" made.");
		
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
		if (alive!=null) alive.setConsumerName(getName());
	}

	@Override
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public boolean isDurable() {
		return durable;
	}

	@Override
	public void setDurable(boolean durable) {
		this.durable = durable;
	}
}
