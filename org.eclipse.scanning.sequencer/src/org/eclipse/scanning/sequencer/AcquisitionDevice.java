package org.eclipse.scanning.sequencer;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

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

	
	private ScanModel        model;
	private DetectorRunner   detectors;
	private DetectorReader   readers;
	private ReentrantLock    lock;
	private Condition        paused;
	private volatile boolean awaitPaused;
	
	AcquisitionDevice() {
		this.lock   = new ReentrantLock();
		this.paused = lock.newCondition();
	}
	
	@Override
	public void configure(ScanModel model) throws ScanningException {
		this.model = model;
		detectors = new DetectorRunner(model.getDetectors());
		readers   = new DetectorReader(model.getDetectors());
	}

	@Override
	public void run() throws ScanningException {
		
		if (model.getPositionIterator()==null) throw new ScanningException("The model must contain some points to scan!");
		
		try {
	        final IPositioner positioner = scanningService.createPositioner(deviceService);
	        
	        for (IPosition pos : model.getPositionIterator()) {
	        	if (awaitPaused) paused.await();

	        	positioner.setPosition(pos);   // moveTo
	        	readers.latch();               // Wait for the previous read out to return, if any
	        	detectors.run(pos);            // GDA8: collectData() / GDA9: run() for Malcolm
	        	readers.run(pos, false);       // Do not block on the readout, move to the next position immeadiately.
		        	
	        }
	        
	        // On the last iteration we must wait for the final readout.
        	readers.latch();                   // Wait for the previous read out to return, if any

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
		try {
			lock.lockInterruptibly();
		} catch (Exception ne) {
			throw new ScanningException(ne);
		}
		
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
