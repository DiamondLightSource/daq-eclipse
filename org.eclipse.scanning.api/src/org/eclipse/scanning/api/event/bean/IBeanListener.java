package org.eclipse.scanning.api.event.bean;

import java.util.EventListener;

public interface IBeanListener<T> extends EventListener {

	/**
	 * Called when any bean is changed and published	
	 * @param evt
	 */
	void beanChangePerformed(BeanEvent<T> evt);
	
	/**
	 * Optionally the bean class may be defined which provides
	 * a hint as to how to deserialize the string.
	 * 
	 * NOTE: In GDA9 and later most objects pass through a serialization
	 * layer which also adds the bundle and class information. Therefore
	 * it is not needed to provide an implementation of the bean class.
	 * 
	 * @return
	 */
	default Class<T> getBeanClass() {
		return null;
	}
}
