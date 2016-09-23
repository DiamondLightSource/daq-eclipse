package org.eclipse.scanning.api.event.scan;

import org.eclipse.scanning.api.ITerminatable.TerminationPreference;

public enum DeviceAction {

	VALIDATE,
	
	CONFIGURE, 
	
	RUN, 
	
	ABORT, 
	
	RESET,
	
	SET,
	
	/**
	 * Stop a move if one is currently running.
	 */
	TCONTROLLED,
	
	/**
	 * Stop a move if one is currently running.
	 */
	TPANIC, 
	
	/**
	 * Called to set a device as activated or not.
	 * NOTE This does not make it run in a scan directly, it just marks it as being included
	 * in new scans submitted to the server.
	 */
	ACTIVATE,
	
	/**
	 * Disable device, stopping all activity.
	 */
	DISABLE;
	
	public static DeviceAction as(TerminationPreference pref) {
		switch(pref) {
		case CONTROLLED:
			return TCONTROLLED;
		case PANIC:
			return TPANIC;
		default:
			return null;
		}
	}
	
	public boolean isTerminate() {
		return this==TCONTROLLED || this==TPANIC;
	}

	public TerminationPreference to() {
		switch(this) {
		case TCONTROLLED:
			return TerminationPreference.CONTROLLED;
		case TPANIC:
			return TerminationPreference.PANIC;
		default:
			return null;
		}
	}

}
