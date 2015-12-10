package org.eclipse.scanning.sequencer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
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
	private DetectorReader   readers;
	
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
		readers   = new DetectorReader(model.getDetectors());
		setState(DeviceState.READY);
	}

	@Override
	public void run() throws ScanningException {
		
		if (model.getPositionIterator()==null) throw new ScanningException("The model must contain some points to scan!");
		
		try {
	        final IPositioner positioner = scanningService.createPositioner(deviceService);
	        
    		setState(DeviceState.RUNNING);
    		
	        for (IPosition pos : model.getPositionIterator()) {
	        	
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
	        	
	        	// Run the position
	        	positioner.setPosition(pos);   // moveTo
	        	readers.await();               // Wait for the previous read out to return, if any
	        	detectors.run(pos);            // GDA8: collectData() / GDA9: run() for Malcolm
	        	readers.run(pos, false);       // Do not block on the readout, move to the next position immediately.
		        	
	        }
	        
	        // On the last iteration we must wait for the final readout.
        	readers.await();                   // Wait for the previous read out to return, if any
        	setState(DeviceState.READY);
        	
		} catch (ScanningException s) {
			throw s;
		} catch (Exception ne) {
			throw new ScanningException(ne);
		}
	}

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
