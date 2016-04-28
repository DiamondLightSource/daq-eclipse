package org.eclipse.scanning.api.scan.event;

import java.util.EventListener;

import org.eclipse.scanning.api.scan.ScanningException;


/**
 * A listener which is fired before and after a device is run.
 * 
 * @author Matthew Gerring
 *
 */
public interface IRunListener extends EventListener {

	/**
	 * Called before a run() is made on the device. Can
	 * be used to modify the model before a given run of the device.
	 * @param evt
	 * @throws scanning exception which will terminate the scan
	 */
	default void runWillPerform(RunEvent evt) throws ScanningException {
		// default implementation does nothing, subclasses should override as necessary
	};
	
	/**
	 * Used to notify that a given device has been run.
	 * @param evt
	 * @throws scanning exception which will terminate the scan
	 */
	default void runPerformed(RunEvent evt) throws ScanningException {
		// default implementation does nothing, subclasses should override as necessary
	};
	
	/**
	 * Called before a run() is made on the device. Can
	 * be used to modify the model before a given run of the device.
	 * @param evt
	 * @throws scanning exception which will terminate the scan
	 */
	default void writeWillPerform(RunEvent evt) throws ScanningException {
		// default implementation does nothing, subclasses should override as necessary
	};
	
	/**
	 * Used to notify that a given device as been run.
	 * @param evt
	 * @throws scanning exception which will terminate the scan
	 */
	default void writePerformed(RunEvent evt) throws ScanningException {
		// default implementation does nothing, subclasses should override as necessary
	};
}
