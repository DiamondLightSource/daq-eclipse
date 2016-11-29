package org.eclipse.scanning.api.device;

import java.util.List;

/**
 * 
 * This service holds avaliable watchdogs and if they are
 * active will start them for a given IRunnableDevice.
 * 
 * @author Matthew Gerring
 *
 */
public interface IDeviceWatchdogService {

	/**
	 * Call to add a watchdog to a scan
	 * @param dog
	 */
	void register(IDeviceWatchdog dog);
	
	/**
	 * Call to remove a watchdog from a scan
	 * @param dog
	 */
	void unregister(IDeviceWatchdog dog);
	
	/**
	 * Initiate a list of dogs to run with 
	 * the device. These then implement @ScanStart, @PointStart, @ScanFinally
	 * as required to participate themselves in the running of the device
	 * and watch the process.
	 * 
	 * This process actually makes new dogs from the active list which means that
	 * the IRunnableDevice<?> passed in is unique to that list.
	 * 
	 * @param device
	 * @return list of objects which may be added to the scan and will be processed
	 * by their annotations.
	 */
	List<IDeviceWatchdog> create(IPausableDevice<?> device);
	

}
