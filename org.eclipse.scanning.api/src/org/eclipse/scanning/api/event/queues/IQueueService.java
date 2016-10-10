package org.eclipse.scanning.api.event.queues;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.queues.beans.QueueBean;
import org.eclipse.scanning.api.event.queues.beans.Queueable;

/**
 * The IQueueService provides an interface for the unattended management of 
 * {@link IEventService} queues.
 * 
 * Two types of queue may be created, started, stopped or destroyed:
 * - job-queue - acting on {@link QueueBean}s
 * - active-queue - acting on {@link QueueAtom}s
 * 
 * The job-queue is treated as a top-level queue - there should only be one 
 * created per IQueueService instance. The active-queue is where the actual 
 * experiment processing should be done; as such there can be any number of 
 * these in parent-child relationships, with the job-queue at the root of the 
 * tree.
 * 
 * Methods are provided for interacting with these queues, to submit, re-order 
 * or remove beans, to pause or terminate processes. The status of beans 
 * within queues and queues themselves can also be monitored.
 * 
 * TODO Add queue pausing, re-ordering, removal.
 * 
 * Matt writes: This TODO should be easy to do.
 *              Reorder, replace, move are available on IQueueConnection. 
 *              Pause is done by broadcasting a pause topic, something like:
 <pre>
    IPublisher<PauseBean> pauser = eservice.createPublisher(submitter.getUri(), IEventService.CMD_TOPIC);
	PauseBean pbean = new PauseBean();
	pbean.setQueueName(consumer.getSubmitQueueName());
	pauser.broadcast(pbean);
 </pre>
 
 *  
 * @author Michael Wharmby
 *
 */

public interface IQueueService {
	
	public static final String JOB_QUEUE = "job-queue";
	public static final String ACTIVE_QUEUE = "active-queue";
	
	public static final String HEARTBEAT_TOPIC_SUFFIX = ".heartbeat.topic";
	
	/**
	 * Initialise the queue service. This should ensure the service is capable
	 * of creating new {@link IQueue} instances and initialise the job queue.
	 * 
	 * @throws EventException if it was not possible to create the job queue.
	 */
	public void init() throws EventException;
	
	/**
	 * Method to tidy up after the method when the service is to be shutdown.
	 * 
	 * @throws EventException
	 */
	public void disposeService() throws EventException;
	
	/**
	 * Start the job queue consumer.
	 * 
	 * @throws EventException in case it was not possible to start the consumer.
	 */
	public void start() throws EventException;
	
	/**
	 * Stop the job queue consumer and any dependent active queue consumers 
	 * gracefully. If force is true, consumers will be killed rather than 
	 * stopped (this will leave jobs in the submit queues).
	 * 
	 * @param force True if all consumers are to be killed.
	 * @throws EventException if consumers could not be stopped.
	 */
	public void stop(boolean force) throws EventException;
	
	/**
	 * Create a new active queue instance and register it with the service. 
	 * This first should also clear the queues/topics to make sure they're 
	 * genuinely clean.
	 * 
	 * @return String name of the active queue registered.
	 * @throws EventException if it was not possible to create the active queue.
	 */
	public String registerNewActiveQueue() throws EventException;
	
	/**
	 * Remove a defunct active queue from the map of registered queues. If the 
	 * queue contains jobs, attempt to gracefully stop & dispose of the queue 
	 * first. Use force to forcibly remove the queue.
	 * 
	 * @param queueID String ID  of queue to be deregistered.
	 * @param force True if consumer should be killed rather than stopped.
	 * @throws EventException if the given active queue could not be found or
	 * 						  problems were encountered stopping the consumer.
	 */
	public void deRegisterActiveQueue(String queueID, boolean force) throws EventException;
	
	/**
	 * Start a registered active queue consumer.
	 * 
	 * @param queueID String ID of active queue registered.
	 * @throws EventException if the active queue cannot be started.
	 */
	public void startActiveQueue(String queueID) throws EventException ;
	
	/**
	 * Stop a registered active queue gracefully. If force is true, consumers 
	 * will be killed rather than stopped (this will leave jobs in the submit 
	 * queue).
	 * 
	 * @param queueID String ID of registered queue.
	 * @param force True if all consumers are to be killed.
	 * @throws EventException if consumers could not be stopped.
	 */
	public void stopActiveQueue(String queueID, boolean force) throws EventException ;
	
	/**
	 * Return the current status of the queue.
	 * 
	 * @param queueID String ID of the queue.
	 * @return QueueStatus report the current state of the queue.
	 */
	public QueueStatus getQueueStatus(String queueID);
	
	/**
	 * Clear a given queue and disconnect it's consumer. Sets 
	 * {@link QueueStatus} to DISPOSED.
	 * 
	 * @param queueID String ID of registered queue.
	 * @param nullify boolean true if the queue service should make the IQueue 
	 * 		  object null
	 */
	public void disposeQueue(String queueID, boolean nullify) throws EventException ;
	
	/**
	 * Stop the consumer and optionally disconnect the consumer (this is a 
	 * dirty dispose) or call System.exit() on the consumer (will pull down
	 * the house). This method has the possibility of leaving jobs on the 
	 * queues. Sets {@link QueueStatus} to KILLED.
	 * 
	 * @param queueID String ID of registered queue.
	 * @param disconnect boolean, disconnect the consumer. 
	 * @param exitProcess boolean, if true calls System.exit(0)
	 */
	public void killQueue(String queueID, boolean disconnect, boolean exitProcess) throws EventException;
	
	/** 
	 * Submit an atom/bean for processing to a given consumer through it's 
	 * submission queue.
	 * 
	 * @param atomBean T queue object to submit. 
	 * @param submitQ String name of queue to submit queue object to.
	 * @throws EventException In case the atom/bean is rejected.
	 */
	public <T extends Queueable> void submit(T atomBean, String submitQ) throws EventException;
	
	/**
	 * Submit a bean extending {@link QueueBean} into the job queue. 
	 * 
	 * @param bean {@link QueueBean} to be submitted.
	 * @throws EventException In case the bean is rejected.
	 */
	public default void jobQueueSubmit(QueueBean bean) throws EventException {
		submit(bean, getJobQueue().getSubmissionQueueName());
	}

	/**
	 * Submit an atom extending {@link QueueAtom} into the given active queue. 
	 * 
	 * @param bean {@link QueueAtom} to be submitted.
	 * @param queueID String ID of registered queue.
	 * @throws EventException In case the atom is rejected.
	 */
	public default void activeQueueSubmit(QueueAtom atom, String queueID) throws EventException {
		submit(atom, getActiveQueue(queueID).getSubmissionQueueName());
	}
	
	/**
	 * Terminate the operation of the given atom/bean using the specified 
	 * status topic.
	 * 
	 * @param atomBean T queue object to stop.
	 * @param statusT String status topic to publish termination request to.
	 * @throws EventException In case termination is unsuccessful.
	 */
	@Deprecated
	public <T extends Queueable> void terminate(T atomBean, String statusT) throws EventException;

	/**
	 * Convenience method to terminate operation of a bean in the job queue.
	 * 
	 * @param bean {@link QueueBean} bean to be terminated.
	 * @throws EventException In case the termination fails.
	 */
	public default void jobQueueTerminate(QueueBean bean) throws EventException {
		terminate(bean, getJobQueue().getStatusTopicName());
	}
	
	/**
	 * Terminate the operation of an atom in the given active queue.
	 * 
	 * @param atom {@link QueueAtom} bean to be terminated.
	 * @param queueID String ID of registered queue.
	 * @throws EventException In case the termination fails.
	 */
	public default void activeQueueTerminate(QueueAtom atom, String queueID) throws EventException {
		terminate(atom, getActiveQueue(queueID).getStatusTopicName());
	}

	/**
	 * Get the {@link IProcessCreator} used as the runner for all 
	 * active-queues.
	 * 
	 * @return Current {@link IProcessCreator} used to configure all 
	 *         active-queues.
	 */
	public IProcessCreator<QueueBean> getJobQueueProcessor();
	
	/**
	 * Change the {@link IProcessCreator} used as the runner for all 
	 * active-queues.
	 * 
	 * @param procCreate {@link IProcessCreator} to be used to configure all 
	 *        active-queues.
	 * @throws EventException if trying to set after service started.
	 */
	public void setJobQueueProcessor(IProcessCreator<QueueBean> procCreate) throws EventException;
	
	/**
	 * Get the {@link IProcessCreator} used as the runner for all 
	 * active-queues.
	 * 
	 * @return Current {@link IProcessCreator} used to configure all 
	 *         active-queues.
	 */
	public IProcessCreator<QueueAtom> getActiveQueueProcessor();
	
	/**
	 * Change the {@link IProcessCreator} used as the runner for all 
	 * active-queues.
	 * 
	 * @param procCreate {@link IProcessCreator} to be used to configure all 
	 *        active-queues.
	 * @throws EventException if trying to set after service started.
	 */
	public void setActiveQueueProcessor(IProcessCreator<QueueAtom> procCreate) throws EventException;
	
	/**
	 * Get current state of all beans in the named queue.
	 * 
	 * @param queueID String ID of registered queue.
	 * @return List of beans as they appear in the status queue.
	 */
	public default List<? extends Queueable> getStatusSet(String queueID) throws EventException {
		if (queueID.equals(getJobQueueID())) return getJobQueueStatusSet();
		else if (getAllActiveQueueIDs().contains(queueID)) return getActiveQueueStatusSet(queueID);
		else throw new IllegalArgumentException("Queue ID not found in registry");
	}
	
	/**
	 * Get current state of all beans in the named queue.
	 * 
	 * @param queueID String ID of registered queue.
	 * @return List of beans as they appear in the status queue.
	 */
	public List<QueueBean> getJobQueueStatusSet() throws EventException;
	
	/**
	 * Get current state of all beans in the named queue.
	 * 
	 * @param queueID String ID of registered queue.
	 * @return List of beans as they appear in the status queue.
	 */
	public List<QueueAtom> getActiveQueueStatusSet(String queueID) throws EventException;
	
	/**
	 * Return an {@link ISubscriber} object pre-configured to listen for 
	 * heartbeats of a given queue.
	 * 
	 * @param queueID String ID of registered queue.
	 * @return A heartbeat monitor for this queue.
	 * @throws EventException if the heartbeat monitor could not be created.
	 */
	public IHeartbeatMonitor getHeartMonitor(String queueID) throws EventException;
	
	/**
	 * Get the job queue managed by this IQueueService instance.
	 * 
	 * @return The requested job queue, implementing {@link IQueue}.
	 */
	public IQueue<QueueBean> getJobQueue();
	
	/**
	 * Get the queueID of the job queue managed by this service.
	 * 
	 * @return String queueID of job queue.
	 */
	public String getJobQueueID();
	
	/**
	 * Return a list of all registered active queues.
	 * 
	 * @return List of strings of active queues registered with the service.
	 */
	public List<String> getAllActiveQueueIDs();
	
	/**
	 * Report whether given active queue is registered with service.
	 * 
	 * @param queueID String ID of registered queue. 
	 * @return true if queueID found in registry.
	 */
	public boolean isActiveQueueRegistered(String queueID);
	
	/**
	 * Get a particular registered active queue from the queue registry.
	 * 
	 * @param queueID String ID of registered queue.
	 * @return The requested active queue instance, implementing {@link IQueue}.
	 */
	public  IQueue<QueueAtom> getActiveQueue(String queueID);
	
	/**
	 * Get the current registry of all active queues, with queueIDs as the key 
	 * field.
	 * 
	 * @return Map of queueID (String) keys with registered queue (IQueue)
	 */
	public Map<String, IQueue<QueueAtom>> getAllActiveQueues();
	
	/**
	 * Get the IEventService instance used for generating all queue infrastructure.
	 *  
	 * @return IEventService instance.
	 */
	public IEventService getEventService();
	
	/**
	 * Return the base name for all queues managed by this IQueueService.
	 * 
	 * @return String queue base name.
	 */
	public String getQueueRoot();
	
	/**
	 * Change the base name used by all queues managed by this IQueueService.
	 * Should not be possible to change this while service is started. Changing
	 * the queueRoot should also change the heartbeat and command topic names.
	 * 
	 * @param queueRoot String queue base name.
	 * @throws EventException If attempting to change whilst service started.
	 */
	public void setQueueRoot(String queueRoot) throws EventException;
	
	/**
	 * Return name of topic on which heartbeats of queues associated with this 
	 * service will be published.
	 * 
	 * @return String name of heartbeat topic for this service.
	 */
	public String getHeartbeatTopicName();
	
	/**
	 * Return base name for the destination (topic/queue) where commands to 
	 * consumers associated with this service should be published.
	 * 
	 * @return String name of command topic for this service.
	 */
	public String getCommandDestinationRoot();
	
	/**
	 * Return the URI of the broker storing the queues.
	 * 
	 * @return URI to the queue broker.
	 */
	public URI getURI();
	
	/**
	 * Return the String path of the URI used to configure this service.
	 * 
	 * @return String of the URI to the queue broker. 
	 */
	public String getURIString();
	
	/**
	 * Change the URI of the broker storing the queues.
	 * Should not be possible to change this while service is started.
	 * 
	 * @param uri URI of new queue server.
	 * @throws EventException If attempting to change whilst service started.
	 */
	public void setURI(URI uri) throws EventException;
	
	/**
	 * Change the URI of the broker storing the queues with a String.
	 * Should not be possible to change this while service is started.
	 * 
	 * @param uri String URI of new queue server.
	 * @throws EventException If attempting to change whilst service started.
	 */
	public void setURI(String uri) throws EventException;
	
	/**
	 * Return whether the queue service is currently running.
	 * 
	 * @return true if queue service running.
	 */
	public boolean isActive();

}
