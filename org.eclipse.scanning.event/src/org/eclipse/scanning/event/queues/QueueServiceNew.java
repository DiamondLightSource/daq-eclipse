package org.eclipse.scanning.event.queues;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.IQueueNew;
import org.eclipse.scanning.api.event.queues.IQueueServiceNew;
import org.eclipse.scanning.api.event.queues.QueueStatus;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.queues.beans.QueueBean;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueServiceNew implements IQueueServiceNew {
	
	private static final Logger logger = LoggerFactory.getLogger(QueueServiceNew.class);
	
	private String queueRoot, uriString, heartbeatTopicName, commandSetName, 
		commandTopicName, jobQueueID;
	private URI uri;
	private boolean active = false, init = false;
	
	private IQueueNew<QueueBean> jobQueue;//FIXME Change QueueBean to an Interface
	private Map<String, IQueueNew<QueueAtom>> activeQueueRegister;
	
	/**
	 * No argument constructor for OSGi
	 */
	public QueueServiceNew() {
		
	}
	
	/**
	 * Constructor for tests
	 */
	public QueueServiceNew(String queueRoot, URI uri) {
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
		jobQueue = new QueueNew<>(jobQueueID, uri, 
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
			deRegisterActiveQueue(aqID, true);
		}

		//Dispose the job queue
		disposeQueue(getJobQueueID(), true);
		
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
			logger.warn("Job queue is not active.");
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
//FIXME			killQueue(getJobQueueID(), true, false);
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
		String aqID = queueRoot+ACTIVE_QUEUE_PREFIX+activeQueueRegister.size()+"-"+randInt+ACTIVE_QUEUE_SUFFIX;
		
		//Create active-queue, add to register & return the active-queue ID
		IQueueNew<QueueAtom> activeQueue = new QueueNew<>(aqID, uri, 
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
		IQueueNew<QueueAtom> activeQueue = getActiveQueue(queueID);
		if (activeQueue.getStatus().isActive()) {
			throw new EventException("Active-queue " + queueID +" still running - cannot deregister.");
		}
		
		//Disconnect remaining queue processes and remove from map
		disposeQueue(queueID, true);
		activeQueueRegister.remove(queueID);
	}
	
	@Override
	public boolean isActiveQueueRegistered(String queueID) {
		return activeQueueRegister.containsKey(queueID);
	}
	
	@Override
	public void startActiveQueue(String queueID) throws EventException {
		//Check active-queue is not already running
		IQueueNew<QueueAtom> activeQueue = getActiveQueue(queueID);
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
		IQueueNew<QueueAtom> activeQueue = getActiveQueue(queueID);
		if (!activeQueue.getStatus().isActive()) {
			logger.warn("Active-queue "+queueID+" is not active.");
			return;
		}
		
		//Kill/stop the job queue
		if (force) {
//FIXME			killQueue(queueID, true, false);
		} else {
			activeQueue.stop();
		}
	}
	
	@Override
	public void disposeQueue(String queueID, boolean nullify) throws EventException {
		//Check active-queue is not already running
		IQueueNew<? extends Queueable> queue = getQueue(queueID);
		if (queue.getStatus().isActive()) throw new EventException("Queue is currently running. Cannot dispose.");
		else if (queue.getStatus() == QueueStatus.DISPOSED) {
			logger.warn("Active-queue "+queueID+" has already been disposed.");
			return;
		}
		
		//Clear queues: in previous iteration found that...
		queue.clearQueues(); //...status queue clear, but submit not...
		queue.disconnect();
		boolean isClear = queue.clearQueues();//... submit queue now clear.
		if (!isClear) throw new EventException("Failed to clear queues when disposing "+queueID);
		
		//Nullify if required
		if (nullify) {
			if (queueID.equals(jobQueue.getQueueID())) { 
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
	public IQueueNew<QueueBean> getJobQueue() {
		return jobQueue;
	}
	
	@Override
	public IQueueNew<QueueAtom> getActiveQueue(String queueID) {
		if (isActiveQueueRegistered(queueID)) return activeQueueRegister.get(queueID);
		throw new IllegalArgumentException("Queue ID "+queueID+" not found in registry");
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

		//Update the destinations
		heartbeatTopicName = queueRoot+HEARTBEAT_TOPIC_SUFFIX;
		commandSetName = queueRoot+COMMAND_SET_SUFFIX;
		commandTopicName = queueRoot+COMMAND_TOPIC_SUFFIX;

		//Update job-queue
		jobQueueID = queueRoot+JOB_QUEUE_SUFFIX;
		jobQueue = new QueueNew<>(jobQueueID, uri, heartbeatTopicName, commandSetName, commandTopicName);
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

		//Update job-queue
		jobQueue = new QueueNew<>(jobQueueID, uri, 
				heartbeatTopicName, commandSetName, commandTopicName);
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
