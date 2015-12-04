package org.eclipse.scanning.sequencer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.AbstractScanner;
import org.eclipse.scanning.api.scan.IPositioner;
import org.eclipse.scanning.api.scan.IRunnableDevice;
import org.eclipse.scanning.api.scan.ScanModel;
import org.eclipse.scanning.api.scan.ScanningException;

/**
 * This scan does a standard GDA scan at each point. If a given point is a 
 * MalcolmDevice, that device will be configured and run for its given point.
 * 
 * The levels of the scannables at the position will be taken into
 * account and the position reached using a positioner then the 
 * scanners run.
 * 
 * @author Matthew Gerring
 *
 * @param <T>
 */
class DescretePointScanner extends AbstractScanner<ScanModel> {

	
	private ScanModel model;
	
	@Override
	public void configure(ScanModel model) throws ScanningException {
		this.model = model;
	}

	@Override
	public void run() throws ScanningException {
		
		if (model.getPositionIterator()==null) throw new ScanningException("The model must contain some points to scan!");
		
        final IPositioner positioner = scanningService.createPositioner(deviceService);
        
        for (IPosition pos : model.getPositionIterator()) {
        	positioner.setPosition(pos);
        	collect();
        	// TODO FIXME - Detector runner?
		}
	}

	private void collect() {
		
		ExecutorService eservice = createService();
        
	}

	private ExecutorService createService() {
		int processors = Runtime.getRuntime().availableProcessors();
		return new ThreadPoolExecutor(processors,             /* number of motors to move at the same time. */
                processors*2,                                 /* max size current tasks. */
                5, TimeUnit.SECONDS,                          /* timeout after - does this need spring config? */
                new ArrayBlockingQueue<Runnable>(1000, true), /* max 1000+ncores motors to a level */
                new ThreadPoolExecutor.AbortPolicy());

	}
	
	private class CollectTask implements Callable<Boolean> {

		private IRunnableDevice<?> detector;

		public CollectTask(IRunnableDevice<?> det) {
			this.detector = det;
		}

		@Override
		public Boolean call() throws Exception {
			
			// TODO Not sure if zebra needs configure called again
			// or if multiple runs() may happen.
			
			detector.run();
			return Boolean.TRUE;
		}
		
	}


	@Override
	public void abort() throws ScanningException {
		throw new ScanningException("Not implemented!");
	}

	@Override
	public void pause() throws ScanningException {
		throw new ScanningException("Not implemented!");
	}

	@Override
	public void resume() throws ScanningException {
		throw new ScanningException("Not implemented!");
	}
}
