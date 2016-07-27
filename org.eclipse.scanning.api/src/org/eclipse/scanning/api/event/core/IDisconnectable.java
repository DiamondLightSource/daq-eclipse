package org.eclipse.scanning.api.event.core;

import org.eclipse.scanning.api.event.EventException;

/**
 * 
 * Any object which has a disconnect may implement this interface.
 * 
 * @author Matthew Gerring
 *
 */
public interface IDisconnectable {

	/**
	 * Call to disconnect any resources which we no longer need.
	 * The resource may have timed out so it might not be connected, 
	 * in that case it silently returns.
	 * 
	 * @throws EventException if resource could not be disconnected. 
	 */
	public void disconnect() throws EventException ;
}
