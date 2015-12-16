package org.eclipse.scanning.sequencer;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.IWritableDetector;
import org.eclipse.scanning.api.scan.IRunnableDevice;
import org.eclipse.scanning.api.scan.ScanningException;

/**
 * 
 * Reads detectors in a task.
 * 
 * This is the equivalent to the GDA8 readout() called on multiple
 * detectors with multiple threads and waiting for isBusy to be unset.
 * The latch method waits for the pool to exit if the run method is
 * called in non-blocking mode.
 * 
 * @author Matthew Gerring
 *
 */
final class DetectorReader extends DetectorRunner {

	DetectorReader(Collection<IRunnableDevice<?>> detectors) {	
		super(detectors);
	}

	@Override
	protected Callable<IPosition> create(IRunnableDevice<?> device, IPosition position) throws ScanningException {
		if (!(device instanceof IWritableDetector<?>)) return null;
		return new ReadTask((IWritableDetector<?>)device, position);
	}

	public class ReadTask implements Callable<IPosition> {

		private IWritableDetector<?> detector;
		private IPosition            position;

		public ReadTask(IWritableDetector<?> detector, IPosition position) {
			this.detector = detector;
			this.position = position;
		}

		@Override
		public IPosition call() throws Exception {
			detector.write();
			return position;
		}

	}

}
