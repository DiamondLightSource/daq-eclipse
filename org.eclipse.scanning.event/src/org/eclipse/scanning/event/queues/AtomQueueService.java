package org.eclipse.scanning.event.queues;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.alive.KillBean;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.queues.IHeartbeatMonitor;
import org.eclipse.scanning.api.event.queues.IQueue;
import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.QueueStatus;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.queues.beans.QueueBean;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.processors.AtomQueueProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AtomQueueService provides an implementation of IQueueService interface, 
 * specifically acting on {@link QueueBean}s and {@link QueueAtom}s. The 
 * service is dependent on the {@link IEventService} to provide the 
 * underlying queue architecture.
 * 
 * A single top-level job-queue {@link IQueue} instance - a queue of beans 
 * extending {@link QueueBean}s - is created on initialisation. The generic 
 * {@link QueueProcessorCreator} is used to create a bean processor dependent
 * on the type of bean passed to it. Processing only begins when the service is
 * started.
 * 
 * Methods are also provided for the on-the-fly creation/disposal of 
 * active-queues - {@link IQueue} instances containing beans extending 
 * {@link QueueAtom}s. Again the generic {@link QueueProcessorCreator} is used 
 * for processor creation. Again processing only happens when the active-queue
 * is started.
 * 
 * At shutdown, queues should be first stopped (with or without force) before 
 * being disposed. Stopping the queue leads to a termination of any running 
 * processes. The disposeService() method will stop and dispose all queues in 
 * the service working through the active-queues and then the job-queue.
 * 
 * It is expected that the {@link AtomQueueProcessor} will call the 
 * registerNewActiveQueue() method to create child queues. The processor will 
 * then be responsible for submitting, starting, stopping etc. of that queue.
 * The number of active-queues is only limited by the maximum value of an int 
 * (2^32).
 * 
 * Methods are provided on the queue service to allow external processes to 
 * interrogate the running of the queues.
 * 
 * @author Michael Wharmby
 *
 */
public class AtomQueueService implements IQueueService {
	
	private static final Logger logger = LoggerFactory.getLogger(AtomQueueService.class);
	
	private static IEventService eventService;
	private String uriString;
	private URI uri;
	
	private String queueRoot, heartbeatTopicName, commandTopicName, commandQueueName;
	private int nrActiveQueues = 0;
	private IProcessCreator<QueueAtom> activeQueueProcessor = new QueueProcessCreator<QueueAtom>(true);
	private IProcessCreator<QueueBean> jobQueueProcessor = new QueueProcessCreator<QueueBean>(true);
	
	private IQueue<QueueBean> jobQueue;
	private Map<String, IQueue<QueueAtom>> activeQueues = new HashMap<String, IQueue<QueueAtom>>();
	
	private boolean active = false;
	
	/**
	 * No argument constructor for OSGi
	 */
	public AtomQueueService() {
		
	}
	
	/**
	 * Constructor for tests
	 */
	public AtomQueueService(String queueRoot, URI uri) {
		this.queueRoot = queueRoot;
		this.uri = uri;
		uriString = uri.toString();
	}
	
	@Override
	public void init() throws EventException {
		//Get the OSGi configured IEventService and check it's non-null. 
		eventService = QueueServicesHolder.getEventService();
		if (eventService == null) throw new IllegalStateException("EventService not set");
		
		//Check remaining configuration
		if (queueRoot == null) throw new IllegalStateException("Queue root has not been specified");
		if (uri == null) throw new IllegalStateException("URI has not been specified");
		
		//Set the service heartbeat & command destinations
		heartbeatTopicName = queueRoot+HEARTBEAT_TOPIC_SUFFIX;
		commandTopicName = queueRoot+COMMAND_TOPIC_SUFFIX;
		commandQueueName = queueRoot+COMMAND_QUEUE_SUFFIX;
		
		//Determine ID of Job Queue
		String jqID = queueRoot+"."+JOB_QUEUE;
		
		//Create a fully configured job queue & set the runner
		jobQueue = new Queue<QueueBean>(jqID, heartbeatTopicName, commandTopicName, commandQueueName, uri);
		jobQueue.setProcessRunner(jobQueueProcessor);
	}
	
	@Override
	public void disposeService() throws EventException {
		//Stop the job queue if service is up
		if (active) stop(true);
		
		//Remove any remaining active queues
		for (String aqID : getAllActiveQueueIDs()) {
			deRegisterActiveQueue(aqID, true);
		}
		
		//Dispose the job queue
		disposeQueue(getJobQueueID(), true);
	}

	@Override
	public void start() throws EventException {
		if (!jobQueue.getQueueStatus().isStartable()) {
			throw new EventException("Job queue not startable - Status: " + jobQueue.getQueueStatus());
		}
		if (jobQueue.getQueueStatus().isActive()) {
			logger.warn("Job queue is already active.");
			return;
		}
		
		//Last step is to start the job queue and mark the service alive
		jobQueue.getConsumer().start();
		jobQueue.setQueueStatus(QueueStatus.STARTED);
		
		//Mark service as up
		active = true;
	}
	
	@Override
	public void stop(boolean force) throws EventException {
		if (!jobQueue.getQueueStatus().isActive()) {
			logger.warn("Job queue is not active.");
			return;
		}
		
		//Deregister all existing active queues.
		if (!activeQueues.isEmpty()) {
			//Create a new HashSet here as the deRegister method changes activeQueues
			Set<String> qIDSet = new HashSet<String>(activeQueues.keySet());
			for (String qID : qIDSet) {
				deRegisterActiveQueue(qID, force);
			}
		}
		
		//Kill/stop the job queue
		if (force) {
			killQueue(getJobQueueID(), true, false);
		} else {
			jobQueue.getConsumer().stop();
			jobQueue.setQueueStatus(QueueStatus.STOPPED);
		}
		
		//Mark the service as down
		active = false;
	}

	@Override
	public String registerNewActiveQueue() throws EventException {
		if (!active) throw new EventException("Queue service not started.");
		
		//Get an ID and the queue names for new active queue
		String aqID = generateActiveQueueID();
		
		//Create a fully configured active queue, purge queues for safety & set runner
		IQueue<QueueAtom> activeQueue = new Queue<QueueAtom>(aqID, heartbeatTopicName, commandTopicName, commandQueueName, uri);
		activeQueue.clearQueues();
		activeQueue.setProcessRunner(activeQueueProcessor);
		
		//Add to registry and increment number of registered queues
		activeQueues.put(aqID, activeQueue);
		nrActiveQueues = activeQueues.size();
		
		return aqID;
	}

	@Override
	public void deRegisterActiveQueue(String queueID, boolean force)
			throws EventException {
		if (!active) throw new EventException("Queue service not started.");
		
		//Get the queue and check that it's not started
		IQueue<QueueAtom> activeQueue = getActiveQueue(queueID);
		if (activeQueue.getQueueStatus().isActive()) {
			logger.warn("Stopping active queue " + queueID + " whilst still active.");
			stopActiveQueue(queueID, force);
		}
		
		//Disconnect remaining queue processes and remove from map
		disposeQueue(queueID, true);
		activeQueues.remove(queueID);
		nrActiveQueues = activeQueues.size();
	}

	@Override
	public void startActiveQueue(String queueID) throws EventException {
		IQueue<QueueAtom> activeQueue = getActiveQueue(queueID);
		if (!activeQueue.getQueueStatus().isStartable()) {
			throw new EventException("Active queue not startable - Status: " + activeQueue.getQueueStatus());
		}
		if (activeQueue.getQueueStatus().isActive()) {
			logger.warn("Active queue is already active.");
			return;
		}
		
		activeQueue.getConsumer().start();
		activeQueue.setQueueStatus(QueueStatus.STARTED);
	}

	@Override
	public void stopActiveQueue(String queueID, boolean force)
			throws EventException {
		IQueue<QueueAtom> activeQueue = getActiveQueue(queueID);
		if (!activeQueue.getQueueStatus().isActive()) {
			logger.warn("Active queue is not active.");
			return;
		}
		
		//Kill/stop the job queue
		if (force) {
			killQueue(queueID, true, false);
		} else {
			activeQueue.getConsumer().stop();
			activeQueue.setQueueStatus(QueueStatus.STOPPED);
		}
	}

	@Override
	public QueueStatus getQueueStatus(String queueID) {
		return getQueueFromString(queueID).getQueueStatus();
	}

	@Override
	public void disposeQueue(String queueID, boolean nullify) throws EventException {
		//Ensures consumer and subscriber are both disconnected
		IQueue<? extends Queueable> queue = getQueueFromString(queueID);
		if (queue.getQueueStatus().isActive()) throw new EventException("Queue is currently running. Cannot dispose.");
		
		//In previous iteration found that...
		queue.clearQueues(); //...status queue clear, but submit not...
		queue.disconnect();
		boolean isClear = queue.clearQueues();//... submit queue now clear.
		if (!isClear) throw new EventException("Failed to clear queues when disposing "+queueID);
		
		queue.setQueueStatus(QueueStatus.DISPOSED);
		if (nullify) {
			if (queueID.equals(jobQueue.getQueueID())) jobQueue = null;
			else if (activeQueues.containsKey(queueID)) {//Prevent adding "non-existent queueID" = null
				activeQueues.put(queueID, null);
			}
		}
	}

	@Override
	public void killQueue(String queueID, boolean disconnect,
			boolean exitProcess) throws EventException {
		IPublisher<KillBean> killer;
		
		//Get the consumer ID for this queue
		UUID consumerID = getQueueFromString(queueID).getConsumerID();
		
		KillBean knife = new KillBean();
		knife.setConsumerId(consumerID);
		knife.setDisconnect(disconnect);
		knife.setExitProcess(exitProcess);
		
		killer = eventService.createPublisher(uri, commandTopicName);
		killer.broadcast(knife);
		killer.disconnect();
		
		getQueueFromString(queueID).setQueueStatus(QueueStatus.KILLED);
	}
	
	@Override
	public <T extends Queueable> void submit(T atomBean, String submitQ) throws EventException {
		//Prepare the atom/bean for submission
		atomBean.setStatus(Status.SUBMITTED);
		try {
			//FIXME Only set if not set already 
			atomBean.setHostName(InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException ex) {
			throw new EventException("Failed to set hostname on bean. " + ex.getMessage());
		}

		//Create a submitter and submit the atom
		ISubmitter<T> submitter = eventService.createSubmitter(uri, submitQ);
		submitter.submit(atomBean);
		submitter.disconnect();
	}

	@Deprecated
	@Override
	public <T extends Queueable> void terminate(T atomBean, String statusT) throws EventException {
		//Set up a publisher to send terminated beans with
		IPublisher<T> terminator = eventService.createPublisher(uri, statusT);

		//Set the bean status, publish it & get rid of the terminator
		atomBean.setStatus(Status.REQUEST_TERMINATE);
		terminator.broadcast(atomBean);
		terminator.disconnect();
	}

	@Override
	public IProcessCreator<QueueBean> getJobQueueProcessor() {
		return jobQueueProcessor;
	}

	@Override
	public void setJobQueueProcessor(IProcessCreator<QueueBean> proCreate)
			throws EventException {
		if (active) throw new EventException("Cannot change job-queue processor when service started.");
		jobQueueProcessor = proCreate;
	}

	@Override
	public IProcessCreator<QueueAtom> getActiveQueueProcessor() {
		return activeQueueProcessor;
	}

	@Override
	public void setActiveQueueProcessor(IProcessCreator<QueueAtom> proCreate)
			throws EventException {
		if (active) throw new EventException("Cannot change active-queue processor when service started.");
		activeQueueProcessor = proCreate;
	}

	@Override
	public List<QueueBean> getJobQueueStatusSet() throws EventException {
		return jobQueue.getConsumer().getStatusSet();
	}

	@Override
	public List<QueueAtom> getActiveQueueStatusSet(String queueID)
			throws EventException {
		return getActiveQueue(queueID).getConsumer().getStatusSet();
	}

	@Override
	public IHeartbeatMonitor getHeartMonitor(String queueID) throws EventException {
		return new HeartbeatMonitor(uri, heartbeatTopicName, queueID, this);
	}
	
	@Override
	public IQueue<QueueBean> getJobQueue() {
		return jobQueue;
	}
	
	@Override
	public String getJobQueueID() {
		return jobQueue.getQueueID();
	}

	@Override
	public List<String> getAllActiveQueueIDs() {
		return new ArrayList<String>(activeQueues.keySet());
	}

	@Override
	public boolean isActiveQueueRegistered(String queueID) {
		return activeQueues.containsKey(queueID);
	}

	@Override
	public IQueue<QueueAtom> getActiveQueue(String queueID) {
		if (isActiveQueueRegistered(queueID)) return activeQueues.get(queueID);
		throw new IllegalArgumentException("Queue ID not found in registry");
	}

	@Override
	public Map<String, IQueue<QueueAtom>> getAllActiveQueues() {
		return activeQueues;
	}

	@Override
	//TODO This can be removed as we have the QueueServicesHolder
	public IEventService getEventService() {
		return eventService;
	}

	@Override
	public String getQueueRoot() {
		return queueRoot;
	}

	@Override
	public void setQueueRoot(String queueRoot) throws EventException {
		if (active) throw new UnsupportedOperationException("Cannot change queue root whilst queue service is running");
		this.queueRoot = queueRoot;
		
		//Update the command & heartbeat topics too
		heartbeatTopicName = queueRoot+HEARTBEAT_TOPIC_SUFFIX;
		commandTopicName = queueRoot;
	}

	@Override
	public String getHeartbeatTopicName() {
		return heartbeatTopicName;
	}

	@Override
	public String getCommandTopicName() {
		return commandTopicName;
	}
	
	@Override
	public String getCommandQueueName() {
		return commandQueueName;
	}

	@Override
	public URI getURI() {
		return uri;
	}
	
	@Override
	public String getURIString() {
		return uriString;
	}

	@Override
	public void setURI(URI uri) throws EventException {
		if (active) throw new UnsupportedOperationException("Cannot change URI whilst queue service is running");
		this.uri = uri;
		uriString = uri.toString();
	}
	
	@Override
	public void setURI(String uri) throws EventException {
		try {
			setURI(new URI(uri));
			uriString = uri;
		} catch (URISyntaxException usEx) {
			throw new EventException(usEx);
		}
	}
	
	@Override
	public boolean isActive() {
		return active;
	}

	private String generateActiveQueueID() {
		nrActiveQueues = activeQueues.size();
		return queueRoot + "." + ACTIVE_QUEUE + "-" + nrActiveQueues;
	}
	
	private IQueue<? extends Queueable> getQueueFromString(String queueID) {
		if (queueID.equals(getJobQueueID())) {
			return getJobQueue();
		} else if (isActiveQueueRegistered(queueID)) {
			return getActiveQueue(queueID);
		} else {
			throw new IllegalArgumentException("Queue ID not found in registry");
		}
	}
}
