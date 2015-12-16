package org.eclipse.scanning.api.scan;

import org.eclipse.scanning.api.ILevel;
import org.eclipse.scanning.api.INameable;


/**
 * 
 * Anatomy of a CPU scan (non-malcolm)
 * 
 * Making GDA8 Detectors work with GDA9 (org.eclipse.scanning) requires usage of the
 * GDA8 IHardwareConnectionService. This service returns IReadableDetector for the class
 * gda.device.Detector and IScanner for gda.px.detector.Detector. In the later case
 * the model provides the file path and omegaStart required.
 *  
 *  ___________
 *  |         |
 * _|         |___________  collectData() Tell detector to collect
 *            ___________
 *            |         |
 * ___________|         |_  readout() Tell detector to readout
 * 
 *            ________
 *            |      |
 * ___________|      |____  moveTo()  Scannables move motors to new position
 * 
 * The readable detector is designed to be read out manually by the scanning
 * system. Malcolm detectors are not read out manually by the scanning but
 * because they implement IScanner, they can be configured and run during the
 * scanning.
 * 
 * @author fcp94556
 *
 * @param <T> Class of model required by detector to configure it.
 */
public interface IWritableDetector<T> extends IRunnableDevice<T>, INameable, ILevel {
	
	/**
	 * For GDA9 detectors, returns a boolean for the readout completed, true for complete, false for error.
	 * Generally detectors running in GDA9 will be IDetector.
	 * 
	 * For GDA8 detectors, the readout will be overridden.
	 * 
	 * @return usually true for a sucessfull readout
	 * @throws DeviceException
	 */
	boolean write() throws ScanningException;

}
