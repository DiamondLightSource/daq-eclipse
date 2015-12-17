package org.eclipse.scanning.api.malcolm.event;

import java.util.EventListener;

/**
 * 
 * This is a plain old event which can be used to get the
 * events during a scan. 
 * 
 * @author Matthew Gerring
 *
 */
public interface IMalcolmListener<T> extends EventListener {

	/**
	 * Called when Malcolm notifies the service that something happened.
	 * @param e
	 */
	public void eventPerformed(MalcolmEvent<T> e);
}
