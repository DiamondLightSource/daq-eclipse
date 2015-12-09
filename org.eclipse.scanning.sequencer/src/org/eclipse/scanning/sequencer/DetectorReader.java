package org.eclipse.scanning.sequencer;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.IReadableDetector;
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
		if (!(device instanceof IReadableDetector<?>)) return null;
		return new ReadTask((IReadableDetector<?>)device, position);
	}

	public class ReadTask implements Callable<IPosition> {

		private IReadableDetector<?> detector;
		private IPosition            position;

		public ReadTask(IReadableDetector<?> detector, IPosition position) {
			this.detector = detector;
			this.position = position;
		}

		@Override
		public IPosition call() throws Exception {
			detector.read();
			return position;
		}

	}

}
