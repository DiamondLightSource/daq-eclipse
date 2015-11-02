package org.eclipse.scanning.api.event.scan;

/**
 * The state that the scanning system may be in.
 * @see org.eclipse.scanning.api.event.scan.malcolm.api.State which is the state that a Malcolm device may be in, not the same as this state.
 * @see gda.scan.Scan.ScanStatus which is the state for Acquisition scans and might be the same thing as this state TBD.
 * 
 * @author Matthew Gerring
 *
 */
public enum State {

	FAULT, IDLE, CONFIGURING, READY, RUNNING, PAUSING, PAUSED, ABORTING, ABORTED;

}
