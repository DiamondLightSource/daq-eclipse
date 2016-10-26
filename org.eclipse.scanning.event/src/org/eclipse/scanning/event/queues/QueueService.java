package org.eclipse.scanning.event.queues;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.queues.IQueue;
import org.eclipse.scanning.api.event.queues.IQueueControllerService;
import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.queues.beans.QueueBean;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.event.queues.beans.TaskBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AtomQueueService provides an implementation of {@link IQueueService}.
 * The service requires a URI and a queueRoot String as configuration. The URI
 * is used to specify the broker which will be used to run create 
 * {@link IEventService} objects and the queueRoot String is used as a starting
 * point to name {@link IQueue} objects.
 * 
 * On starting, the service creates a job-queue {@link IQueue} object which 
 * processes {@link QueueBean}s (i.e. {@link TaskBean}s in the design). The 
 * service has methods to register new active-queue {@link IQueue} object on 
 * the fly, with the names for these based on the queueRoot String and a random
 * number to ensure queue names do not collide.
 * 
 * All queues are configured to share the same heartbeat & command destinations
 * to allow control of the service (these are also based on the queueRoot 
 * String with common suffixes appended).
 * 
 * Users should be able to interact with the service directly, and therefore 
 * the job-queue. However individual active-queues should work autonomously.
 * Interaction with the queue is provided through 
 * {@link IQueueControllerService}.
 * 
 * To start the service, after instantiation a queueRoot & URI should be 
 * provided. init() can then be called, leaving the service in a state where 
 * the start() and stop() methods can be used to activate/deactivate bean 
 * processing. To shutdown the service, call disposeService()  
 * 
 * 
 * @author Michael Wharmby
 *
 */
public class QueueService implements IQueueService {
	
	private static final Logger logger = LoggerFactory.getLogger(QueueService.class);
	
	private String queueRoot, uriString, heartbeatTopicName, commandSetName, 
		commandTopicName, jobQueueID;
	private URI uri;
	private boolean active = false, init = false;
	
	private IQueue<QueueBean> jobQueue;//FIXME Change QueueBean to an Interface
	private Map<String, IQueue<QueueAtom>> activeQueueRegister;
	
	/**
	 * No argument constructor for OSGi
	 */
	public QueueService() {
		
	}
	
	/**
	 * Constructor for tests
	 */
	public QueueService(String queueRoot, URI uri) {
		this.queueRoot = queueRoot;
		this.uri = uri;
	}

	@Override
	public void init() throws EventException {
		//Check configuration is present
		if (queueRoot == null) throw new IllegalStateException("Queue root has not been specified");
		if (uri == null) throw new IllegalStateException("URI has not been specified");
		
		//URI & queueRoot are already set, so we need to set their dependent fields
		uriString = uri.toString();
		heartbeatTopicName = queueRoot+HEARTBEAT_TOPIC_SUFFIX;
		commandSetName = queueRoot+COMMAND_SET_SUFFIX;
		commandTopicName = queueRoot+COMMAND_TOPIC_SUFFIX;
		
		//Now we can set up the job-queue
		jobQueueID = queueRoot+JOB_QUEUE_SUFFIX;
		jobQueue = new Queue<QueueBean>(jobQueueID, uri, 
				heartbeatTopicName, commandSetName, commandTopicName);
		
		//Create the active-queues map
		activeQueueRegister = new HashMap<>();
		
		//Mark initialised
		init = true;
	}

	@Override
	public void disposeService() throws EventException {
		//Stop the job queue if service is up
		if (active) stop(true);

		//Remove any remaining active queues
		for (String aqID : getAllActiveQueueIDs()) {
			stopActiveQueue(aqID, true);
			deRegisterActiveQueue(aqID, true);
		}

		//Dispose the job queue
		disposeQueue(jobQueue, true);
		
		//Mark the service not initialised
		init = false;
	}

	@Override
	public void start() throws EventException {
		if (!init) {
			throw new IllegalStateException("QueueService has not been initialised.");
		}
		//Check the job-queue is in a state to start
		if (!jobQueue.getStatus().isStartable()) {
			throw new EventException("Job queue not startable - Status: " + jobQueue.getStatus());
		} else if (isActive() || jobQueue.getStatus().isActive()) {
			logger.warn("Job queue is already active.");
			return;
		}
		//Start the job-queue if it can be
		jobQueue.start();
		
		//Mark service as up
		active = true;
	}

	@Override
	public void stop(boolean force) throws EventException {
		if (!(isActive() || jobQueue.getStatus().isActive())) {
			logger.warn("Job-queue is not active.");
			return;
		}

		//Deregister all existing active queues.
		if (!activeQueueRegister.isEmpty()) {
			//Create a new HashSet here as the deRegister method changes activeQueues
			Set<String> qIDSet = new HashSet<String>(getAllActiveQueueIDs());
			for (String qID : qIDSet) {
				deRegisterActiveQueue(qID, force);
			}
		}
		
		//Kill/stop the job queue
		if (force) {
//FIXME			IQueueControllerService controller = ServicesHolder.getQueueControllerService();
//			controller.killQueue(queueID, true, false);
		} else {
			jobQueue.stop();
		}

		//Mark service as down
		active = false;
	}
	
	@Override
	public String registerNewActiveQueue() throws EventException {
		if (!active) throw new IllegalStateException("Queue service not started.");
		
		//Generate the random name of the queue
		Random randNrGen = new Random();
		String randInt = String.format("%03d", randNrGen.nextInt(999));
		String aqID = queueRoot+ACTIVE_QUEUE_PREFIX+activeQueueRegister.size()+"-"+randInt+ACTIVE_QUEUE_SUFFIX;;
		//And really make sure we don't get any name collisions
		while (activeQueueRegister.containsKey(aqID)) {
			aqID = queueRoot+ACTIVE_QUEUE_PREFIX+activeQueueRegister.size()+"-"+randInt+ACTIVE_QUEUE_SUFFIX;
		}
		
		//Create active-queue, add to register & return the active-queue ID
		IQueue<QueueAtom> activeQueue = new Queue<>(aqID, uri, 
				heartbeatTopicName, commandSetName, commandTopicName);
		activeQueue.clearQueues();
		activeQueueRegister.put(aqID, activeQueue);
		return aqID;
	}
	
	@Override
	public void deRegisterActiveQueue(String queueID, boolean force) 
			throws EventException {
		if (!active) throw new EventException("Queue service not started.");
		
		//Get the queue and check that it's not started
		IQueue<QueueAtom> activeQueue = getActiveQueue(queueID);
		if (activeQueue.getStatus().isActive()) {
			throw new EventException("Active-queue " + queueID +" still running - cannot deregister.");
		}
		
		//Remove remaining queue processes from map
		activeQueueRegister.remove(queueID);
	}
	
	@Override
	public boolean isActiveQueueRegistered(String queueID) {
		return activeQueueRegister.containsKey(queueID);
	}
	
	@Override
	public void startActiveQueue(String queueID) throws EventException {
		//Check active-queue is not already running
		IQueue<QueueAtom> activeQueue = getActiveQueue(queueID);
		if (!activeQueue.getStatus().isStartable()) {
			throw new EventException("Active-queue "+queueID+" is not startable - Status: " + activeQueue.getStatus());
		}
		if (activeQueue.getStatus().isActive()) {
			logger.warn("Active-queue "+queueID+" is already active.");
			return;
		}
		activeQueue.start();
	}
	
	@Override
	public void stopActiveQueue(String queueID, boolean force) 
			throws EventException {
		//Check active-queue is running
		IQueue<QueueAtom> activeQueue = getActiveQueue(queueID);
		if (!activeQueue.getStatus().isActive()) {
			logger.warn("Active-queue "+queueID+" is not active.");
			return;
		}
		
		//Kill/stop the job queue
		if (force) {
//FIXME			IQueueControllerService controller = ServicesHolder.getQueueControllerService();
//			controller.killQueue(queueID, true, false);
		} else {
			activeQueue.stop();
		}
		
		//And dispose the queue afterwards
		disposeQueue(activeQueue, false);
	}
	
	private void disposeQueue(IQueue<? extends Queueable> queue, boolean nullify) throws EventException {
		String queueID = queue.getQueueID();
		
		//Clear queues: in previous iteration found that...
		queue.clearQueues(); //...status queue clear, but submit not...
		queue.disconnect();
		boolean isClear = queue.clearQueues();//... submit queue now clear.
		if (!isClear) throw new EventException("Failed to clear queues when disposing "+queueID);
		
		//Nullify if required
		if (nullify) {
			if (queueID.equals(jobQueueID)) { 
				jobQueue = null;
				jobQueueID = null;
			}
			else if (activeQueueRegister.containsKey(queueID)) {//Prevent adding "non-existent queueID" = null
				activeQueueRegister.put(queueID, null);
			}
		}
	}
	
	@Override
	public Set<String> getAllActiveQueueIDs() {
		return activeQueueRegister.keySet();
	}
	
	@Override
	public IQueue<QueueBean> getJobQueue() {
		return jobQueue;
	}
	
	@Override
	public IQueue<QueueAtom> getActiveQueue(String queueID) throws EventException {
		if (isActiveQueueRegistered(queueID)) return activeQueueRegister.get(queueID);
		throw new EventException("Queue ID "+queueID+" not found in registry");
	}
	
	@Override
	public String getJobQueueID() {
		return jobQueueID;
	}

	@Override
	public String getQueueRoot() {
		return queueRoot;
	}

	@Override
	public void setQueueRoot(String queueRoot) throws UnsupportedOperationException, EventException {
		if (active) throw new UnsupportedOperationException("Cannot change queue root whilst queue service is running");
		this.queueRoot = queueRoot;

		if (init) {
			//Update the destinations
			heartbeatTopicName = queueRoot+HEARTBEAT_TOPIC_SUFFIX;
			commandSetName = queueRoot+COMMAND_SET_SUFFIX;
			commandTopicName = queueRoot+COMMAND_TOPIC_SUFFIX;

			//Update job-queue
			jobQueueID = queueRoot+JOB_QUEUE_SUFFIX;
			jobQueue = new Queue<>(jobQueueID, uri, heartbeatTopicName, commandSetName, commandTopicName);
		}
	}

	@Override
	public String getHeartbeatTopicName() {
		return heartbeatTopicName;
	}

	@Override
	public String getCommandSetName() {
		return commandSetName;
	}

	@Override
	public String getCommandTopicName() {
		return commandTopicName;
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
	public void setURI(URI uri) throws UnsupportedOperationException, EventException {
		if (active) throw new UnsupportedOperationException("Cannot change URI whilst queue service is running");
		this.uri = uri;
		uriString = uri.toString();

		if (init) {
			//Update job-queue
			jobQueue = new Queue<>(jobQueueID, uri, 
					heartbeatTopicName, commandSetName, commandTopicName);
		}
	}

	@Override
	public void setURI(String uri) throws UnsupportedOperationException, EventException {
		try {
			setURI(new URI(uri));
		} catch (URISyntaxException usEx) {
			throw new EventException(usEx);
		}
	}

	@Override
	public boolean isActive() {
		return active;
	}

}
