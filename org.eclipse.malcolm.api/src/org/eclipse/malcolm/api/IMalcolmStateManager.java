package org.eclipse.malcolm.api;

import java.util.concurrent.TimeUnit;


public interface IMalcolmStateManager {

	/**
	 * 
	 * @return the current state of Malcolm even if 
	 */
	public State getState() throws MalcolmDeviceException ;

	/**
	 * Instruct the device to return once a given state is reached.
	 * @param time or less than 0 to wait infinitely (not recommended)
	 * @param unit
	 * @param ignoredStates
	 * @throws MalcolmDeviceException
	 */
	public State latch(long time, TimeUnit unit, State... ignoredStates) throws MalcolmDeviceException; 
}
