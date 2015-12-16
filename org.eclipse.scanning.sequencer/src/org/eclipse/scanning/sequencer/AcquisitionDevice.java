package org.eclipse.scanning.sequencer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.AbstractRunnableDevice;
import org.eclipse.scanning.api.scan.IPositioner;
import org.eclipse.scanning.api.scan.IRunnableDevice;
import org.eclipse.scanning.api.scan.ScanModel;
import org.eclipse.scanning.api.scan.ScanningException;

/**
 * This device does a standard GDA scan at each point. If a given point is a 
 * MalcolmDevice, that device will be configured and run for its given point.
 * 
 * The levels of the scannables at the position will be taken into
 * account and the position reached using an IPositioner then the 
 * scanners run.
 * 
 * @author Matthew Gerring
 *
 * @param <T>
 */
final class AcquisitionDevice extends AbstractRunnableDevice<ScanModel> {

	// Scanning stuff
	private ScanModel        model;
	private DetectorRunner   detectors;
	private DetectorReader   writers;
	
	/*
	 * Concurrency design recommended by Keith Ralphs after investigating
	 * how to pause and resume a collection cycle using Reentrant locks.
	 * Design requires these three fields.
	 */
	private ReentrantLock    lock;
	private Condition        paused;
	private volatile boolean awaitPaused;
		
	/**
	 * Package private constructor, devices are created by the service.
	 */
	AcquisitionDevice() {
		super();
		this.lock      = new ReentrantLock();
		this.paused    = lock.newCondition();
	}
	
	@Override
	public void configure(ScanModel model) throws ScanningException {
		this.model = model;
		detectors = new DetectorRunner(model.getDetectors());
		writers   = new DetectorReader(model.getDetectors());
		setState(DeviceState.READY);
	}

	@Override
	public void run() throws ScanningException, InterruptedException {
		
		if (model.getPositionIterator()==null) throw new ScanningException("The model must contain some points to scan!");
		
		try {
	        final IPositioner positioner = scanningService.createPositioner(deviceService);
	        
	        // TODO Should we validate the position iterator that all
	        // the positions are valid before running the scan?
	        // It was called limit checking in GDA.
	        // Sometimes logic is needed to implement collision avoidance.
	        
    		setState(DeviceState.RUNNING);
    		
	        for (IPosition pos : model.getPositionIterator()) {
	        	
	        	// Check if we are paused, blocks until we are not
	        	checkPaused();
	        	
	        	// TODO Some validation on each point
	        	// perhaps replacing atPointStart(..)
	        	// Whether to deal with atLineStart() and atPointStart()
	        	
	        	// Run the position
	        	positioner.setPosition(pos);   // moveTo
	        	
	        	// check that beam is up. In the past this has been done with 
	        	// scannables at a given level that block until they are happy.
	        	
	        	writers.await();               // Wait for the previous read out to return, if any
	        	detectors.run(pos);            // GDA8: collectData() / GDA9: run() for Malcolm
	        	writers.run(pos, false);       // Do not block on the readout, move to the next position immediately.
		        	
	        	// TODO Event for actual data written, for analysis to listen to.
	        }
	        
	        // On the last iteration we must wait for the final readout.
        	writers.await();                   // Wait for the previous read out to return, if any
        	setState(DeviceState.READY);
        	
		} catch (ScanningException s) {
			throw s;
		} catch (InterruptedException i) {
			throw i;
		} catch (Exception ne) {
			throw new ScanningException(ne);
		}
	}

	private void checkPaused() throws Exception {
		
    	// Check the locking using a condition
    	if(!lock.tryLock(1, TimeUnit.SECONDS)) {
    		throw new ScanningException(this, "Internal Error - Could not obtain lock to run device!");    		
    	}
    	try {
        	if (awaitPaused) {
        		setState(DeviceState.PAUSED);
        		paused.await();
        		setState(DeviceState.RUNNING);
        	}
    	} finally {
    		lock.unlock();
    	}
		
	}

	// TODO Abort can be interpreted different ways. As 'skip' for short exposure experiments
	// it finishes the current frame, writes file and stops motors. For long exposure it might
	// need to stop the detector exposing further.
	
	// TODO Abort can stop everything, including detectors motors and file writing immediately.
	// Should the model define the behaviour of abort for a given detector? This would allow
	// abort to be configurable for different detectors.
	
	@Override
	public void abort() throws ScanningException {
		throw new ScanningException("Not implemented!");
	}

	@Override
	public void pause() throws ScanningException {
		
		if (getState() != DeviceState.RUNNING) {
			throw new ScanningException(this, getName()+" is not running and cannot be paused!");
		}
		try {
			lock.lockInterruptibly();
		} catch (Exception ne) {
			throw new ScanningException(ne);
		}
		
		setState(DeviceState.PAUSING);
		try {
			awaitPaused = true;
			for (IRunnableDevice<?> device : model.getDetectors()) {
				device.pause();
			}
			
		} catch (ScanningException s) {
			throw s;
		} catch (Exception ne) {
			throw new ScanningException(ne);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void resume() throws ScanningException {
		
		if (getState() != DeviceState.PAUSED) {
			throw new ScanningException(this, getName()+" is not paused and cannot be resumed!");
		}
		try {
			lock.lockInterruptibly();
		} catch (Exception ne) {
			throw new ScanningException(ne);
		}
		
		try {
			awaitPaused = false;
			for (IRunnableDevice<?> device : model.getDetectors()) {
				device.resume();
			}
			paused.signalAll();
			
		} catch (ScanningException s) {
			throw s;
		} finally {
			lock.unlock();
		}
	}
}
