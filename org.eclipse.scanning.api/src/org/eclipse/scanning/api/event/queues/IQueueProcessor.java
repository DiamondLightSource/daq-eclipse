package org.eclipse.scanning.api.event.queues;

import java.util.List;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.beans.Queueable;

public interface IQueueProcessor<T extends Queueable> {
	
	public void execute() throws EventException, InterruptedException;
	
	public void terminate() throws EventException;
	
	/**
	 * Return a list of strings of all the atom/bean class names which this 
	 * processor can process.
	 * 
	 * @return List<String> class names of processable atom/bean types.
	 */
	public List<String> getAtomBeanTypes();

}
