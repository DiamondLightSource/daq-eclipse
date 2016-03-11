package org.eclipse.scanning.api.device;

import org.eclipse.scanning.api.scan.ScanningException;

public interface IPauseableDevice<T> extends IRunnableDevice<T> {

	/**
	 * Allowed when the device is in Running state. Will block until the device is in a rest state. 
	 * 
	 * When paused the same thread must call resume() or abort() which has paused or an
	 * IllegalMonitorState Exception will be thrown.
	 */
	public void pause() throws ScanningException;
	
	/**
	 * Allowed when the device is in Paused state. Will block until the device is unpaused.
	 * 
	 * When paused the same thread must call resume() or abort() which has paused or an
	 * 
	 * IllegalMonitorState Exception will be thrown.
	 */
	public void resume() throws ScanningException;
	

}
