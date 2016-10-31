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

	RESETTING,IDLE,EDITING,EDITABLE,SAVING,REVERTING,READY,CONFIGURING,PRERUN,RUNNING,POSTRUN,PAUSED,SEEKING,ABORTING,ABORTED,FAULT,DISABLING,DISABLED;

	/**
	 * The run method may be called
	 * @return
	 */
	public boolean isRunnable() {
		return this==READY;
	}
	
	public boolean isRunning() {
		return this==RUNNING || this==PAUSED || this==SEEKING || this==PRERUN || this==POSTRUN;
	}

	/**
	 * Before run means that the state is before running and not in error.
	 * @return
	 */
	public boolean isBeforeRun() {
		return this==READY || this==IDLE || this == CONFIGURING;
	}

	public boolean isRest() {
		return this==FAULT || this==IDLE || this==CONFIGURING || this==READY || this==ABORTED || this==DISABLED;
	}

	public boolean isAbortable() {
		return this==RUNNING || this==CONFIGURING || this==PAUSED || this==SEEKING || this==READY || this==PRERUN || this==POSTRUN;
	}
	
	public boolean isResetable() {
		return this==FAULT || this==ABORTED || this==DISABLED || this==READY;
	}

	public boolean isTransient() {
		return this==RUNNING || this==CONFIGURING || this==ABORTING || this==SEEKING || this==DISABLING || this==PRERUN || this==POSTRUN;
	}

	public boolean isRestState() {
		return this==IDLE || this==READY || this==FAULT || this==ABORTED || this==DISABLED;
	}
}
