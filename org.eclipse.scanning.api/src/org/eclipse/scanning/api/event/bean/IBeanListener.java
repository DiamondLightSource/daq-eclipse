package org.eclipse.scanning.api.event.bean;

import java.util.EventListener;

public interface IBeanListener<T> extends EventListener {

	/**
	 * Called when any bean is changed and published	
	 * @param evt
	 */
	void beanChangePerformed(BeanEvent<T> evt);
}
