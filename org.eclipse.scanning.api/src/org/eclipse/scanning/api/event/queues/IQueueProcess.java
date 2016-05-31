package org.eclipse.scanning.api.event.queues;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;

/**
 * Process used in the queue service to run the instructions provided by a 
 * queue bean. By separating the process and the processor, it is possible to 
 * configure the behaviour of the process at runtime, allowing processing of 
 * beans from a heterogenous queue. 
 * 
 * @author Michael Wharmby
 *
 * @param <T> Bean type passed by consumer (this might be a super-type of the 
 *            bean to be processed in the case of heterogenous queues).
 */
public interface IQueueProcess <T extends Queueable> extends IConsumerProcess<T> {

	/**
	 * Default method, implementing method of {@link IConsumerProcess}. 
	 * Instructs the {@link IQueueProcessor} to start processing. 
	 */
	public default void execute() throws EventException, InterruptedException {
		getProcessor().execute();
	}
	
	/**
	 * Default method, implementing method of {@link IConsumerProcess}. 
	 * Instructs the {@link IQueueProcessor} to temporarily halt processing.
	 */
	public default void pause() throws EventException {
		getProcessor().pause();
	}
	
	/**
	 * Default method, implementing method of {@link IConsumerProcess}.
	 * Instructs {@link IQueueProcessor} to restart processing after a pause.
	 */
	public default void resume() throws EventException {
		getProcessor().resume();
	}

	/**
	 * Default method, implementing method of {@link IConsumerProcess}.
	 * Instructs the {@link IQueueProcessor} to abort processing.
	 */
	public default void terminate() throws EventException {
		getProcessor().terminate();
	}

	/**
	 * Convenience method to call broadcast with only {@link Status} argument.
	 * 
	 * @param newStatus Status the bean has just reached.
	 * @throws EventException In case broadcasting fails.
	 */
	public default void broadcast(Status newStatus) throws EventException {
		broadcast(newStatus, null, null);
	}

	/**
	 * Convenience method to call broadcast with only percent complete 
	 * argument.
	 * 
	 * @param newPercent The value percent complete should be set to.
	 * @throws EventException In case broadcasting fails.
	 */
	public default void broadcast(double newPercent) throws EventException {
		broadcast(null, newPercent, null);
	}
	
	/**
	 * Convenience method to call broadcast with percent complete and 
	 * {@link Status} arguments.
	 * 
	 * @param newStatus Status the bean has just reached.
	 * @param newPercent The value percent complete should be set to.
	 * @throws EventException In case broadcasting fails.
	 */
	public default void broadcast(Status newStatus, Double newPercent) throws EventException {
		broadcast(newStatus, newPercent);
	}

	/**
	 * Broadcast the new status, update previous status, percent complete and 
	 * message of the bean associated with this process.
	 * 
	 * @param newStatus Status the bean has just reached.
	 * @param newPercent The value percent complete should be set to.
	 * @throws EventException In case broadcasting fails.
	 */
	public void broadcast(Status newStatus, Double newPercent, String message) throws EventException;

	/**
	 * Return the queue processor which will be used to process the bean 
	 * associated with this process.
	 * 
	 * @return {@link IQueueProcessor} to process the bean.
	 */
	public IQueueProcessor<T, ? extends Queueable> getProcessor();

}
