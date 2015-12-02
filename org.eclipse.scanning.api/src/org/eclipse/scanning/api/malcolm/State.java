package org.eclipse.scanning.api.malcolm;

/**
 * The state that a Malcolm device can be in. This is distinct from a scanning event 
 * because the State for the scanning system is not dependent on the Malcolm state machine.
 * 
 * @author Matthew Gerring
 *
 */
public enum State {

	// TODO not finished state list
	FAULT, IDLE, CONFIGURING, READY, RUNNING, PAUSING, PAUSED, ABORTING, ABORTED;

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

	public boolean isPostRun() {
		return this==IDLE||this==READY||this==FAULT;
	}
}
