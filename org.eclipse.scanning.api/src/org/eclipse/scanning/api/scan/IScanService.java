package org.eclipse.scanning.api.scan;

import java.util.Collection;

import org.eclipse.scanning.api.device.IRunnableDeviceService;

/**
 * 
 * This interface is used to provide custom scanning methods 
 * to the IRunnableDeviceService.
 * 
 * @author Matthew Gerring
 *
 */
public interface IScanService extends IRunnableDeviceService {

	/**
	 * Used to register a scan participant. Once registered any scan
	 * created will use the particpant.
	 * 
	 * @param device
	 */
	void addScanParticipant(IScanParticipant device);

	
	/**
	 * Used to remove a scan participant. Once registered any scan
	 * created will use the particpant it must be removed to stop this.
	 * 
	 * @param device
	 */
	void removeScanParticipant(IScanParticipant device);

	/**
	 * The list of objects to be run as particpants with the scan.
	 * @return
	 */
	Collection<IScanParticipant> getScanParticipants();
}
