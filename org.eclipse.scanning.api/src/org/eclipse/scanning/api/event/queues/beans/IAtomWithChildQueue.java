package org.eclipse.scanning.api.event.queues.beans;

/**
 * Interface allowing messages which are specific to the behaviour & operation
 * of the queue to be passed through the queue hierarchy.
 * 
 * TODO Make IQueueable
 * 
 * @author Michael Wharmby
 *
 */
public interface IAtomWithChildQueue extends IQueueable {
	
	/**
	 * Get the string reporting changes in the child queue, affecting this 
	 * atom/bean.
	 * 
	 * @return String report of child queue state.
	 */
	public String getQueueMessage();
	
	/**
	 * Set the string reporting changes in the child queue, affecting this 
	 * atom/bean.
	 * 
	 * @param String report of child queue state.
	 */
	public void setQueueMessage(String msg);

}
