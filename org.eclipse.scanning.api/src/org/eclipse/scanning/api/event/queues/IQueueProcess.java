package org.eclipse.scanning.api.event.queues;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.queues.beans.Queueable;

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
public interface IQueueProcess <T extends Queueable> extends IConsumerProcess<T>, IQueueProgressBroadcaster {

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
	 * Return the queue processor which will be used to process the bean 
	 * associated with this process.
	 * 
	 * @return {@link IQueueProcessor} to process the bean.
	 */
	public IQueueProcessor<? extends Queueable> getProcessor();

}
