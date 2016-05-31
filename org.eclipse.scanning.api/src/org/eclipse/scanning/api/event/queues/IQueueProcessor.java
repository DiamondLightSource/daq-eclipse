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
	 * Return the currently configured {@link IQueueProcess} managing this 
	 * IQueueProcessor.
	 * 
	 * @return IQueueProcess responsible for this IQueueProcessor.
	 */
	public IQueueProcess<? extends Queueable> getQueueProcess();
	
	/**
	 * Configures the queue process which this processor will use to inform of 
	 * state changes of the process. Bean should be set as part or soon 
	 * after this call, to ensure correct bean is being accessed. Queue process
	 * cannot be changed after execution has begun.
	 * 
	 * @param broadcaster IQueueProgressBroadcaster to use for broadcasting 
	 *                    updates.
	 * @throws EventException if attempt to change queue process after 
	 *         execution has started.
	 */
	public void setQueueProcess(IQueueProcess<? extends Queueable> process) throws EventException ;
	
	/**
	 * Default method to cast and get the bean held by the current 
	 * {@link IQueueProcess} to the type operated on by this IQueueProcessor. 
	 * Type safety is maintained by comparing the classes of the bean from the 
	 * {@link IQueueProcess} and the class for which this IQueueProcessor is 
	 * configured.
	 * 
	 * @return P Instance of {@link QueueProcess} bean cast for this 
	 *         IQueueProcessor.
	 * @throws EventException if the configured and found classes differ.
	 */
	@SuppressWarnings("unchecked")
	public default P getProcessBean() throws EventException {
		if (getQueueProcess().getBean().getClass().equals(getBeanClass())) {
			return (P) getQueueProcess().getBean();
		} else {
			throw new EventException();
		}
	}
}
