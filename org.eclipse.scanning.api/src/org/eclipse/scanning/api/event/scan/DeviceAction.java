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
	TPANIC;

	
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
