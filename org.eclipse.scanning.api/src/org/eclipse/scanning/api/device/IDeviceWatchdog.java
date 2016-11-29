package org.eclipse.scanning.api.device;

import org.eclipse.scanning.api.IModelProvider;
import org.eclipse.scanning.api.device.models.DeviceWatchdogModel;

/**
 * 
 * A watchdog may be started to run with a scan.
 * 
 * It will attempt to pause a scan when topup is about 
 * to happen and restart it after topup has finished.
 * 
 * Once made a watch dog is active if the activate method
 * is called. The deactivate method may be called to stop
 * a given watchdog watching scans.
 * 
 * https://en.wikipedia.org/wiki/Watchdog_timer
 * 
 * NOTE: IDeviceWatchdog concrete class MUST have a no-argument constructor.
 * 
 * @author Matthew Gerring
 *
 */
public interface IDeviceWatchdog extends IModelProvider<DeviceWatchdogModel> {
	
    /**
	 * Make this device active, it will then be used in any scans run
	 * IMPORTANT: Call this method when the object is created in spring to register with the service.
	 */
	void activate();
	
	/**
	 * Called by the framework when a device is created to run with a specific scan.
	 * @param device
	 */
	void setDevice(IPausableDevice<?> device);

	/**
	 * 
	 */
	DeviceWatchdogModel getModel();

	/**
	 * 
	 * @param model
	 */
	void setModel(DeviceWatchdogModel model);
}
