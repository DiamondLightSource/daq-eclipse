package org.eclipse.scanning.api;


/**
 * 
 * Anatomy of a CPU scan (non-malcolm)
 * 
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
 * 
 * 
 * @author fcp94556
 *
 * @param <T>
 */
public interface IDetector<T> extends INameable, ILevel {

	
	/**
	 * Tells the detector to begin to collect a set of data, then returns immediately. Should cause the hardware to
	 * start collecting immediately: if there is any delay then detectors used in the same scan would collect over
	 * different times when beam conditions may differ.
	 * 
	 * @throws Exception
	 */
	void collectData() throws Exception;
	
	/**
	 * Sets the collection time, in seconds, to be used during the next call of collectData.
	 * 
	 * @param time
	 *            the collection time in seconds
	 * @throws DeviceException
	 */
	void setCollectionTime(double time) throws Exception;
	
	/**
	 * Returns the time, in seconds, the detector collects for during the next call to collectData()
	 * 
	 * @return double
	 */
	double getCollectionTime() throws Exception;

	
	/**
	 * For GDA9 detectors, returns a boolean for the readout completed, true for complete, false for error.
	 * Generally detectors running in GDA9 will be IDetector<Boolean>.
	 * 
	 * For GDA8 detectors, the readout will return something which should be written.
	 * Generally detectors running in GDA8 will be IDetector<Object>.
	 * 
	 * @return the data collected
	 * @throws DeviceException
	 */
	T readout() throws Exception;

}
