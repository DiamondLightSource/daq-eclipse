package org.eclipse.scanning.api.event.scan;

/**
 * The state that the scanning system device may be in, for instance a MalcolmDevice.
 * 
 * <img src="./doc/device_state.png" />
 * 
 * @author Matthew Gerring
 *
 */
public enum DeviceState {

	FAULT, IDLE, READY, CONFIGURING, RUNNING, PAUSING, PAUSED, ABORTING, ABORTED; 
//	FAULT, IDLE, READY, CONFIGURING, RUNNING,          PAUSED, ABORTING, ABORTED
//	RESETTING,PRERUN,POSTRUN,REWINDING,DISABLING,DISABLED
	
	//RESETTING,IDLE,READY,CONFIGURING,PRERUN,RUNNING,POSTRUN,PAUSED,REWINDING,ABORTING,ABORTED,FAULT,DISABLING,DISABLED
	/**
	 * The run method may be called
	 * @return
	 */
	public boolean isRunnable() {
		return this==READY;
	}
	
	public boolean isRunning() {
		return this==RUNNING || this==PAUSED || this==PAUSING;
	}

	/**
	 * Before run means that the state is before running and not in error.
	 * @return
	 */
	public boolean isBeforeRun() {
		return this==READY || this==IDLE || this == CONFIGURING;
	}

	public boolean isRest() {
		return this==FAULT || this==IDLE || this == CONFIGURING || this == READY || this == ABORTED;
	}

	public boolean isAbortable() {
		return this==RUNNING || this==CONFIGURING || this == PAUSED || this == PAUSING || this == READY;
	}
	
	public boolean isResetable() {
		return this==FAULT || this==ABORTED;
	}

	public boolean isTransient() {
		return this==RUNNING || this==CONFIGURING || this==ABORTING || this==PAUSING;
	}

	public boolean isRestState() {
		return this==IDLE||this==READY||this==FAULT||this==ABORTED;
	}
}
