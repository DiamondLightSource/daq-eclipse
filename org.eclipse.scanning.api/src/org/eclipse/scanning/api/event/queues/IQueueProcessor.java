package org.eclipse.scanning.api.event.queues;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.beans.Queueable;

public interface IQueueProcessor<T extends Queueable> {
	
	public void execute() throws EventException, InterruptedException;
	
	public void terminate() throws EventException;
	
	/**
	 * Return the string name of the atom/bean class which this processor can
	 * process.
	 * 
	 * @return String class name of processable atom/bean type.
	 */
	public String getAtomBeanType();

}
