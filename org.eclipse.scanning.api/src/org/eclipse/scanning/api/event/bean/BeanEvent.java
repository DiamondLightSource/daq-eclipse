package org.eclipse.scanning.api.event.bean;

import java.util.EventObject;

/**
 * General event which can notify of any bean change happening.
 * 
 * @author Matthew Gerring
 *
 */
public class BeanEvent<T> extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -164386744914837339L;


	public BeanEvent(T bean) {
		super(bean);
	}


	@SuppressWarnings("unchecked")
	public T getBean() {
		return (T)getSource();
	}
}
