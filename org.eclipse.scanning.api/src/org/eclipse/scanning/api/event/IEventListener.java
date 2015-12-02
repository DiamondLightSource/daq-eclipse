package org.eclipse.scanning.api.event;

import java.util.EventListener;

public interface IEventListener<T> extends EventListener {

	/**
	 * Class of bean we are listening to
	 * @return
	 */
	public Class<T> getBeanClass();
}
