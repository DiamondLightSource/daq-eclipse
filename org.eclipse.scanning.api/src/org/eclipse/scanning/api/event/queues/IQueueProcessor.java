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
 * @param <T>
 * @param <P>
 */
public interface IQueueProcessor<T extends Queueable, P extends Queueable> {
	
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
	 * Convenience method to return the {@link IQueueProcess} bean cast to the
	 * expected type for this IQueueProcessor (<P>). The method tests the 
	 * correctness of the bean type for casting before performing the cast. 
	 * Warnings for cast checking have thus (safely) been suppressed.
	 * 
	 * @return P bean cast to the type for this IQueueProcessor.
	 * @throws EventException if the bean type found is not supported by this 
	 *         processor.
	 */
	@SuppressWarnings("unchecked")
	public default P bean() throws EventException{
		T bean = getProcess().getBean();
		
		if (bean.getClass() == getBeanClass()) {
			return (P) bean;
		} else {
			throw new EventException("Bean class "+getBeanClass()+" not supported. Expecting "+getBeanClass()); 
		}
	}
	
	/**
	 * Return the {@link IQueueProcess} with which this IQueueProcessor 
	 * instance is associated.
	 * 
	 * @return IQueueProcess controlling this instance.
	 */
	public IQueueProcess<T> getProcess();
	
	/**
	 * Return the class of the bean which this IQueueProcessor can process.
	 * 
	 * @return Class of bean which can be processed.
	 */
	public Class<P> getBeanClass();

}
