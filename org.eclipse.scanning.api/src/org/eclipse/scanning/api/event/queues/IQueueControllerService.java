package org.eclipse.scanning.api.event.queues;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.beans.Queueable;

public interface IQueueControllerService {
	
	/**
	 * Start the {@link IQueueService}.
	 * 
	 * @throws EventException if it was not possible to start the service.
	 */
	public void start() throws EventException;
	
	/**
	 * Stop the {@link IQueueService} gracefully. If force is true, consumers 
	 * will be killed rather than stopped.
	 * 
	 * @param force True if all consumers are to be killed.
	 * @throws EventException if the service could not be stopped.
	 */
	public void stop(boolean force) throws EventException;
	
	public <T extends Queueable> void submit(T bean, String queueID);
	
	public <T extends Queueable>void remove(T bean, String queueID) throws EventException;
	
	public <T extends Queueable>void reorder(T bean, int move, String queueID) throws EventException;
	
	public <T extends Queueable>void pause(T bean, String queueID) throws EventException;
	
	public <T extends Queueable>void resume(T bean, String queueID) throws EventException;
	
	public <T extends Queueable>void terminate(T bean, String queueID) throws EventException;
	
	public void pauseQueue(String queueID) throws EventException;
	
	public void resumeQueue(String queueID) throws EventException;
	
	public void killQueue(String queueID, boolean disconnect,boolean exitProcess) throws EventException;

}
