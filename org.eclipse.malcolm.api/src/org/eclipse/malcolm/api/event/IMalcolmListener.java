package org.eclipse.malcolm.api.event;

import java.util.EventListener;

/**
 * 
 * This is a plain old event which can be used to get the
 * events during a scan. This can be used in addition to 
 * listening to a topic but obviously only works in the 
 * same VM as that running the IMalcolmService to talk to
 * Malcolm.
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
