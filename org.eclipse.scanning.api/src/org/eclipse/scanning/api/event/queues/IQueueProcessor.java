package org.eclipse.scanning.api.event.queues;

import java.util.List;

import org.eclipse.scanning.api.event.EventException;

public interface IQueueProcessor {
	
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
