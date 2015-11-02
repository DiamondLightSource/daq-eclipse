package org.eclipse.scanning.api.event.bean;

import org.eclipse.scanning.api.event.IEventListener;

public interface IBeanListener<T> extends IEventListener<T> {

	/**
	 * Called when any bean is changed and published	
	 * @param evt
	 */
	void beanChangePerformed(BeanEvent<T> evt);
}
