package org.eclipse.scanning.api.event.queues;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.alive.IHeartbeatListener;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.queues.beans.QueueBean;
import org.eclipse.scanning.api.event.queues.beans.Queueable;

/**
 * The IQueueService manages a single top level job queue {@link IQueue} 
 * instance (containing beans extending {@link QueueBean}) and multiple active 
 * queue {@link IQueue} instances (containing beans extending the 
 * {@link QueueAtom} class).
 * 
 * Active queues are created on the fly each time a {@link SubTaskBean} is 
 * processed. This allows a number of tiers of active queues to be generated,
 * currently limited only by the maximum value of an int (2^32).
 * 
 * The IQueueService also provides methods to interact with the queues: 
 * submitting beans, viewing bean statuses, terminating beans,
 * viewing queue heartbeats and killing consumers. Queue interactions rely on
 * an implementation of {@link IEventService}.
 * 
 * TODO Add queue pausing and re-ordering.
 *  
 * @author Michael Wharmby
 *
 */

public interface IQueueService {
	
	public static final String JOB_QUEUE = "job-queue";
	public static final String ACTIVE_QUEUE = "active-queue";
	
	public static final String HEARTBEAT_TOPIC_SUFFIX = ".heartbeat.topic";
	public static final String KILL_TOPIC_SUFFIX = ".kill.topic";
	
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
	 * Submit a bean extending {@link QueueBean} into the job queue. 
	 * 
	 * @param bean {@link QueueBean} to be submitted.
	 * @throws EventException In case the bean is rejected.
	 */
	public void jobQueueSubmit(QueueBean bean) throws EventException;

	/**
	 * Submit an atom extending {@link QueueAtom} into the given active queue. 
	 * 
	 * @param bean {@link QueueAtom} to be submitted.
	 * @param queueID String ID of registered queue.
	 * @throws EventException In case the atom is rejected.
	 */
	public void activeQueueSubmit(QueueAtom atom, String queueID) throws EventException;

	/**
	 * Terminate the operation of a bean in the job queue.
	 * 
	 * @param bean {@link QueueBean} bean to be terminated.
	 * @throws EventException In case the termination fails.
	 */
	public void jobQueueTerminate(QueueBean bean) throws EventException;
	
	/**
	 * Terminate the operation of an atom in the given active queue.
	 * 
	 * @param atom {@link QueueAtom} bean to be terminated.
	 * @param queueID String ID of registered queue.
	 * @throws EventException In case the termination fails.
	 */
	public void activeQueueTerminate(QueueAtom atom, String queueID) throws EventException;

	
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
	 */
	public ISubscriber<IHeartbeatListener> getHeartMonitor(String queueID);
	
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
	 * Should not be possible to change this while service is started.
	 * 
	 * @param queueRoot String queue base name.
	 * @throws EventException If attempting to change whilst service started.
	 */
	public void setQueueRoot(String queueRoot) throws EventException;
	
	/**
	 * Return the URI of the server storing the queues.
	 * 
	 * @return URI to the queue server
	 */
	public URI getURI();
	
	/**
	 * Change the URI of the server storing the queues.
	 * Should not be possible to change this while service is started.
	 * 
	 * @param uri URI of new queue server.
	 * @throws EventException If attempting to change whilst service started.
	 */
	public void setURI(URI uri) throws EventException;

}
