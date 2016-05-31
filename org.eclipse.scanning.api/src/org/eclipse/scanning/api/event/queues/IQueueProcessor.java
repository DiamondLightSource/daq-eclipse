package org.eclipse.scanning.api.event.queues;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.beans.Queueable;

/**
 * IQueueProcessors hold the logic used by the QueueProcess (which is itself 
 * called from the consumer). Each IQueueProcessor is written to process one 
 * bean type, which is that given by the getBeanClass method. Each 
 * IQueueProcessor is associated with an {@link IQueueProcess} which calls 
 * execute, terminate, pause.
 * 
 * @author Michael Wharmby
 *
 * @param <P> The concrete bean type operated on by this processor.
 */
public interface IQueueProcessor<P extends Queueable> {

	/**
	 * Process the data from the bean provided by he {@link IQueueProcess}. 
	 * The bean should initially have a {@link Status} of RUNNING. On 
	 * completion, the bean {@link Status} should be COMPLETE and the percent 
	 * complete = 100%. If an exception occurs, set the bean {@link Status} to 
	 * FAILED.
	 * 
	 * @throws EventException when exceptions arise, caused by the 
	 *         {@link IEventService}/{@link IQueueService} systems
	 * @throws InterruptedException if threads waiting for events are 
	 *         interrrupted during their wait.
	 */
	public void execute() throws EventException, InterruptedException;

	/**
	 * Called when the the consumer has requested {@link IQueueProcess} to 
	 * wait. Set bean {@link Status} to PAUSED.
	 * 
	 * @throws EventException if the pausing logic fails to execute.
	 */
	public void pause() throws EventException;

	/**
	 * Called when the consumer has requested the {@link IQueueProcess} to 
	 * restart processing of the bean. Set bean {@link Status} to RESUMED, 
	 * then RUNNING.
	 * 
	 * @throws EventException if the un-pausing logic fails to execute.
	 */
	public void resume() throws EventException;

	/**
	 * Called when the consumer requires the {@link IQueueProcess} to abort 
	 * processing the current bean.
	 * 
	 * @throws EventException if it was not possible to abort cleanly.
	 */
	public void terminate() throws EventException;

	/**
	 * Return the class of the bean which this IQueueProcessor can process.
	 * 
	 * @return Class of bean which can be processed.
	 */
	public Class<P> getBeanClass();

	/**
	 * Set bean containing the data for this IQueueProcessor. Bean will be cast 
	 * from type <T> to type <P>; the bean type is tested before making this 
	 * cast & an error returned if the given bean is not an instance of <P>.
	 * 
	 * @param bean to be operated on by this processor.
	 * @throws EventException if the given bean type is not supported.
	 */
	public <T extends Queueable> void setProcessBean(T bean) throws EventException;

	/**
	 * Configures the queue process which this processor will use to inform of 
	 * state changes of the process. Bean should be set at the same time, to 
	 * ensure correct bean is being accessed. Queue process cannot be changed 
	 * after execution has begun.
	 * 
	 * @param process IQueueProcess to use for broadcasting updates.
	 * @throws EventException if attempt to change queue process after 
	 *         execution has started.
	 */
	public void setQueueProcess(IQueueProcess<? extends Queueable> process) throws EventException;

	/**
	 * Set boolean executed to indicate start of execution.
	 */
	public void setExecuted();

	/**
	 * Return whether execution has begun.
	 * 
	 * @return true if execution begun.
	 */
	public boolean isExecuted();
}
