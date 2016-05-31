package org.eclipse.scanning.api.event.queues;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.queues.beans.Queueable;

public interface IQueueProcessor<Q extends Queueable, T extends Q> {
	
	public void execute() throws EventException, InterruptedException;
	
	public void terminate() throws EventException;
	
	/**
	 * Return the string name of the atom/bean class which this processor can
	 * process.
	 * 
	 * @return String class name of processable atom/bean type.
	 */
	public String getAtomBeanType();
	
	public IConsumerProcess<Q> getProcess();
	
	public Class<T> getBeanClass();
	
	@SuppressWarnings("unchecked")
	public default T bean() throws EventException{
		Q bean = getProcess().getBean();
		
		if (bean.getClass() == getBeanClass()) {
			return (T) bean;
		} else {
			throw new EventException("Bean class "+getBeanClass()+" not supported. Expecting "+getBeanClass()); 
		}
	}

}
