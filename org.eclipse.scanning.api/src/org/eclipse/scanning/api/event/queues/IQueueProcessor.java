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
	 * Configure this processor with data from the bean held by the 
	 * {@link IQueueProcess}. This method casts a bean of class 
	 * {@link Queueable} to type <P>, but only after performing a check that 
	 * the bean is of the correct concrete type for this processor. Thus type 
	 * safety is not checked by the compiler, but within the code and 
	 * warnings can be suppressed safely.
	 *  
	 *  @throws EventException if the bean type found is not supported by this 
	 *         processor.
	 */
	@SuppressWarnings("unchecked")
	public default void configure(Queueable bean) throws EventException {
		if (bean.getClass() == getBeanClass()) {
			recoverBeanData((P)bean);
		} else {
			throw new EventException("Bean class "+getBeanClass()+" not supported. Expecting "+getBeanClass()); 
		}
	}

	/**
	 * Recover the actual data from the bean supplied to the configure method.
	 * 
	 * @param bean Containing data to be operated on by queue processor.
	 * @throws EventException in case reading the data fails.
	 */
	public void recoverBeanData(P bean) throws EventException;

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
	 * Configures the broadcaster which this processor will use to inform of 
	 * state changes of the process.
	 * 
	 * @param broadcaster IQueueProgressBroadcaster to use for broadcasting 
	 *                    updates.
	 */
	public void setProgressBroadcaster(IQueueProgressBroadcaster broadcaster);

}
