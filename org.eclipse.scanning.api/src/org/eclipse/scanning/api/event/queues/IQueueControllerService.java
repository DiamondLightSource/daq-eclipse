package org.eclipse.scanning.api.event.queues;

import java.util.EventListener;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.queues.beans.Queueable;

/**
 * FIXME Do java-doc
 * @author Michael Wharmby
 *
 */
public interface IQueueControllerService {
	
	public void init();
	
	/**
	 * Start the {@link IQueueService}.
	 * 
	 * @throws EventException - if it was not possible to start the service.
	 */
	public void start() throws EventException;
	
	/**
	 * Stop the {@link IQueueService} gracefully. If force is true, consumers 
	 * will be killed rather than stopped.
	 * 
	 * @param force True if all consumers are to be killed.
	 * @throws EventException - if the service could not be stopped.
	 */
	public void stop(boolean force) throws EventException;
	
	/**
	 * 
	 * @param bean
	 * @param queueID
	 * @throws EventException - if the queueID is unknown or underlying submission system fails.
	 * @throws IllegalArgumentException - if the bean has the wrong type for the given queueID.
	 */
	public <T extends Queueable> void submit(T bean, String queueID) throws EventException;
	
	/**
	 * 
	 * @param bean
	 * @param queueID
	 * @throws EventException - if the queueID is unknown, underlying removal system fails or the bean is not present in the submission queue.
	 * @throws IllegalArgumentException - if the bean has the wrong type for the given queueID.
	 */
	public <T extends Queueable>void remove(T bean, String queueID) throws EventException;
	
	/**
	 * 
	 * @param bean
	 * @param move
	 * @param queueID
	 * @throws EventException - if the queueID is unknown, underlying reordering systems fails or the bean is not present in the submission queue.
	 * @throws IllegalArgumentException - if the bean has the wrong type for the given queueID.
	 */
	public <T extends Queueable>void reorder(T bean, int move, String queueID) throws EventException;
	
	/**
	 * 
	 * @param bean
	 * @param queueID
	 * @throws EventException - if the queueID is unknown, the bean is not in the queue or has not started processing, or the underlying systems fails.
	 * @throws IllegalArgumentException - if the bean has the wrong type for the given queueID.
	 * @throws IllegalStateException - if the bean has already been paused or is only submitted.
	 */
	public <T extends Queueable>void pause(T bean, String queueID) throws EventException;
	
	/**
	 * 
	 * @param bean
	 * @param queueID
	 * @throws EventException - if the queueID is unknown, the bean is not in the queue or has not started processing, or the underlying systems fails.
	 * @throws IllegalArgumentException - if the bean has the wrong type for the given queueID.
	 * @throws IllegalStateException - if the bean has already been resumed or is only submitted.
	 */
	public <T extends Queueable>void resume(T bean, String queueID) throws EventException;
	
	/**
	 * N.B. If bean has not been submitted, it will be removed instead.
	 * 
	 * @param bean
	 * @param queueID
	 * @throws EventException - if the queueID is unknown, the bean is not in the queue or has not started processing, or the underlying systems fails.
	 * @throws IllegalArgumentException - if the bean has the wrong type for the given queueID.
	 * @throws IllegalStateException - if the bean has already been terminated.
	 */
	public <T extends Queueable>void terminate(T bean, String queueID) throws EventException;
	
	/**
	 * 
	 * @param queueID
	 * @throws EventException - if the queueID is unknown or underlying pause systems fails.
	 */
	public void pauseQueue(String queueID) throws EventException;
	
	/**
	 * 
	 * @param queueID
	 * @throws EventException - if the queueID is unknown or underlying resume systems fails.
	 */
	public void resumeQueue(String queueID) throws EventException;
	
	/**
	 * 
	 * @param queueID
	 * @param disconnect
	 * @param exitProcess
	 * @throws EventException - if the queueID is unknown or underlying queue killing systems fails.
	 */
	public void killQueue(String queueID, boolean disconnect,boolean exitProcess) throws EventException;
	
	/**
	 * 
	 * @param queueID
	 * @return
	 * @throws EventException - if the queueID is unknown.
	 */
	public <T extends EventListener> ISubscriber<T> createQueueSubscriber(String queueID) throws EventException;

}
