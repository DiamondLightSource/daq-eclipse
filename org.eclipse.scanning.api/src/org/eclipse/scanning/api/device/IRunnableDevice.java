package org.eclipse.scanning.api.device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.scanning.api.IConfigurable;
import org.eclipse.scanning.api.ILevel;
import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;


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
 * IPointGeneratorService gservice = ...// OSGi<br>
 * StepModel model = parser.getModel("x");<br>
 * Iterable<IPosition> gen = gservice.createGenerator(model)<br>
 * <br>
 * // Now scan the point iterator<br>
 * IDeviceService sservice = ...// OSGi<br>
 * IRunnableDevice<ScanModel> scanner = sservice.createScanner(...);<br>
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
public interface IRunnableDevice<T> extends INameable, ILevel, IConfigurable<T>, IResettableDevice {
	
	/**
	 * 
	 * @return the current device State. This is not the same as the Status of the scan.
	 */
	public DeviceState getDeviceState() throws ScanningException;

	/**
	 * Blocking call to execute the scan. The position specified may be null.
	 * 
	 * @throws ScanningException
	 */
	public void run(IPosition position) throws ScanningException, InterruptedException;
	
	/**
	 * The default implementation of start simply executes run in a thread named using the getName() value.
	 * @param pos
	 * @throws ScanningException
	 * @throws InterruptedException
	 */
	default void start(final IPosition pos) throws ScanningException, InterruptedException {
		
		final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>(1));
		final Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					IRunnableDevice.this.run(pos);
				} catch (ScanningException|InterruptedException e) {
					// If you add an exception type to this catch clause,
					// you must also add an "else if" clause for it inside
					// the "if (!exceptions.isEmpty())" conditional below.
					e.printStackTrace();
					exceptions.add(e);
				}
			}
		}, getName()+" execution thread");
		thread.start();
		
		// We delay by 500ms just so that we can 
		// immediately throw any connection exceptions
		Thread.sleep(500);
		
		// Re-throw any exception from the thread.
		if (!exceptions.isEmpty()) {
			Throwable ex = exceptions.get(0);

			// We must manually match the possible exception types because Java
			// doesn't let us do List<Either<ScanningException, InterruptedException>>.
			if (ex.getClass() == ScanningException.class) {
				throw (ScanningException) ex;

			} else if (ex.getClass() == InterruptedException.class) {
				throw (InterruptedException) ex;

			} else {
				throw new IllegalStateException();
			}
		}
	}
	
	/**
	 * Call to terminate the scan before it has finished.
	 * 
	 * @throws ScanningException
	 */
	public void abort() throws ScanningException;
	
	/**
	 * Latches until this run is complete if it was initiated from a start.
	 * If a device does not have a latch, then this method always throws an exception.
	 * 
	 * @throws ScanningException
	 */
	default void latch() throws ScanningException, InterruptedException {
		throw new ScanningException("Latch is not implemnented for "+getClass().getSimpleName());
	}
	
	/**
	 * If the device is a virtual device which like a scan device controlling other
	 * hardware, it will return true for virtual. Normally hardware which is wrapped by
	 * a single java class will return false. It is not virtual and one instance of the wrapping
	 * class should exist. For standard non-virtual devices the IDeviceService will 
	 * cache the connection to the device such that it only has one connection and configuration.
	 * 
	 * @return
	 */
	default boolean isVirtual() {
		return false;
	}
	
	/**
	 * The model being used for the device.
	 * @return
	 */
	public T getModel();
}
