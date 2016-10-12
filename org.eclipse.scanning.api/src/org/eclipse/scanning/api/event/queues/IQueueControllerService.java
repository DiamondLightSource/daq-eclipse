package org.eclipse.scanning.api.event.queues;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.beans.IQueueable;

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
	
	public <T extends IQueueable> void submit(T bean, String queueID);
	
	public <T extends IQueueable>void remove(T bean, String queueID);
	
	public <T extends IQueueable>void reorder(T bean, int move, String queueID);
	
	public <T extends IQueueable>void pause(T bean, String queueID);
	
	public <T extends IQueueable>void resume(T bean, String queueID);
	
	public <T extends IQueueable>void terminate(T bean, String queueID);
	
	public void pauseQueue(String queueID);
	
	public void resumeQueue(String queueID);
	
	public void killQueue(String queueID, boolean disconnect,boolean exitProcess);

}
