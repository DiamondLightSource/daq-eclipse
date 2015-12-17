package org.eclipse.scanning.api.scan;

import org.eclipse.scanning.api.IConfigurable;
import org.eclipse.scanning.api.ILevel;
import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;


/**
 * 
 * An IDevice is the runner for the whole scan but also for individual
 * detectors. Detectors, for instance an IMalcolmDevice can be run in 
 * the system as if it were an IDetector.
 * 
 * Anatomy of a CPU scan (non-malcolm)
 * 
 *  <br>
 *&nbsp;_________<br>
 *_|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|________  collectData() Tell detector to collect<br>
 *&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;_________<br>
 *_________|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|_  readout() Tell detector to readout<br>
 *&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;_______<br>
 *_________|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|___  moveTo()  Scannables move motors to new position<br>
 * <br>
 *<br>
 * A MalcolmDevice is also an IDetector which may operate with an arbitrary model, usually driving hardware.<br>
 * <br>
 * <usage><code>
 * IParserService pservice = ...// OSGi<br>
 * <br>
 * // Parse the scan command, throws an exception<br>
 * IParser<StepModel> parser = pservice.createParser(...)<br>
 * // e.g. "scan x 0 5 0.1 analyser"<br>
 * <br>
 * // Now use the parser to create a generator<br>
 * IGeneratorService gservice = ...// OSGi<br>
 * StepModel model = parser.getModel("x");<br>
 * Iterable<IPosition> gen = gservice.createGenerator(model)<br>
 * <br>
 * // Now scan the point iterator<br>
 * IScanningService sservice = ...// OSGi<br>
 * IDetector<ScanModel> scanner = sservice.createScanner(...);<br>
 * scanner.configure(model);<br>
 * scanner.run();<br>
 * 
 * </code></usage>
 *
 * <img src="./doc/device_state.png" />
 * 
 * @author Matthew Gerring
 *
 */
public interface IRunnableDevice<T> extends INameable, ILevel, IConfigurable<T> {
	
	/**
	 * 
	 * @return the current device State. This is not the same as the Status of the scan.
	 */
	public DeviceState getState() throws ScanningException ;


	/**
	 * Blocking call to execute the scan
	 * 
	 * @throws ScanningException
	 */
	public void run() throws ScanningException, InterruptedException;
	
	/**
	 * Call to terminate the scan before it has finished.
	 * 
	 * @throws ScanningException
	 */
	public void abort() throws ScanningException;

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
