package org.eclipse.scanning.api.scan;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.event.IRunListener;

/**
 * 
 * A runnable device which also allows the ability to listen to when it
 * will run and after it has run. This allows for instance for a given
 * runnable device to configure it before the run method is called based on the 
 * position that the device will be run at. For instance for a device writing
 * different scan files depending on an outer scan, it can know the position
 * of the outer scan to generate the output file name and then call configure(...)
 * to ensure that the detector will write to the new location.
 * 
 * @author Matthew Gerring
 *
 * @param <T>
 */
public interface IRunnableEventDevice<T> extends IRunnableDevice<T> {

	/**
	 * A listener which notifies when a run of the device will occur and after it
	 * has occurred. This can be used to modify the model before the next run.
	 * 
	 * @throws ScanningException
	 */
	public void addRunListener(IRunListener l) throws ScanningException;
	
	/**
	 * A listener which notifies when a run of the device will occur and after it
	 * has occurred. This can be used to modify the model before the next run.
	 * 
	 * @throws ScanningException
	 */
	public void removeRunListener(IRunListener l) throws ScanningException;

	/**
	 * Called during the scanning to notify the device that the run method will be
	 * called at a given position. This method is called on the same thread that the
	 * run() method is called on, e.g. parallel execution by level in the case of
	 * AcquisitionDevice.
	 * 
	 * @param position
	 */
	public void fireRunWillPerform(IPosition position)throws ScanningException;
	
	/**
	 * Called during the scanning to notify the device that the run method has been
	 * called at a given position. This method is called on the same thread that the
	 * run() method is called on, e.g. parallel execution by level in the case of
	 * AcquisitionDevice.
	 * 
	 * @param position
	 */
	public void fireRunPerformed(IPosition position)throws ScanningException;

}
