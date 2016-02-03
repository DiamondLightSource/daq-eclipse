package org.eclipse.scanning.sequencer;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.AbstractRunnableDevice;
import org.eclipse.scanning.api.scan.IPauseableDevice;
import org.eclipse.scanning.api.scan.IRunnableDevice;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.eclipse.scanning.api.scan.models.ScanModel;

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
	private IPositioner                          positioner;
	private LevelRunner<IRunnableDevice<?>>      runners;
	private LevelRunner<IRunnableDevice<?>>      writers;
	
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
	
	/**
	 * Method to configure the device. It also will check if the
	 * declared devices in the scan are INexusDevice. If they are,
	 * it will hook them up to the file writing if the ScanModel 
	 * file is set. If there is no file set in the model, the scan
	 * will proceed but not write to a nexus file.
	 */
	@Override
	public void configure(ScanModel model) throws ScanningException {
		
		setModel(model);
		setBean(model.getBean()!=null?model.getBean():new ScanBean());
		
		positioner = scanningService.createPositioner();
		if (getModel().getDetectors()!=null) {
			runners = new DeviceRunner(model.getDetectors());
			writers = new DeviceWriter(model.getDetectors());
		} else {
			runners = LevelRunner.createEmptyRunner();
			writers = LevelRunner.createEmptyRunner();
		}
		
		try {
			linkNeXus(model);
		} catch (NexusException e) {
			throw new ScanningException(e);
		}
		
		setState(DeviceState.READY);
	}

	/**
	 * Connects devices involved in the scan with the NeXus file writing
	 * if we are writing NeXus.
	 * 
	 * @param model
	 * @return
	 * @throws NexusException
	 * @throws ScanningException
	 */
	private boolean linkNeXus(ScanModel model) throws NexusException, ScanningException {
		if (model.getFilePath()==null || ServiceHolder.getFactory()==null) return false; // nothing wired 
		NexusFileScanBuilder nexusFileCreator = new NexusFileScanBuilder(getDeviceService());
		return nexusFileCreator.createNexusFile(model);
	}

	@Override
	public void run(IPosition parent) throws ScanningException, InterruptedException {
		
		if (getState()!=DeviceState.READY) throw new ScanningException("The device '"+getName()+"' is not ready. It is in state "+getState());
		
		ScanModel model = getModel();
		if (model.getPositionIterable()==null) throw new ScanningException("The model must contain some points to scan!");
		
		try {
	        
	        // TODO Should we validate the position iterator that all
	        // the positions are valid before running the scan?
	        // It was called limit checking in GDA.
	        // Sometimes logic is needed to implement collision avoidance
    		setState(DeviceState.RUNNING);
    	
    		// We allow monitors which can block a position until a setpoint is
    		// reached or add an extra record to the NeXus file.
    		if (model.getMonitors()!=null) positioner.setMonitors(model.getMonitors());
    		
    		// Set the size and declare a count
    		int size  = 0;
    		int count = 0;
    		for (IPosition unused : model.getPositionIterable()) size++; // Fast even for large stuff

    		// Notify that we will do a run and provide the first position.
        	fireRunWillPerform(model.getPositionIterable().iterator().next());

        	// The scan loop
        	IPosition pos = null; // We want the last point when we are done so don't use foreach
	        for (Iterator<IPosition> it = model.getPositionIterable().iterator(); it.hasNext();) {
				
	        	pos = it.next();
	        	pos.setStepIndex(count);
	        	
	        	// Check if we are paused, blocks until we are not
	        	checkPaused();
	        	
	        	// TODO Some validation on each point
	        	// perhaps replacing atPointStart(..)
	        	// Whether to deal with atLineStart() and atPointStart()
	        	
	        	// Run to the position
	        	positioner.setPosition(pos);   // moveTo in GDA8
	        	
	        	writers.await();               // Wait for the previous read out to return, if any
	        	runners.run(pos);            // GDA8: collectData() / GDA9: run() for Malcolm
	        	writers.run(pos, false);       // Do not block on the readout, move to the next position immediately.
		        		        	
	        	// Send an event about where we are in the scan
	        	positionComplete(pos, count+1, size);
	        	++count;
	        	
	        }
	        
	        // On the last iteration we must wait for the final readout.
        	writers.await();                   // Wait for the previous read out to return, if any
        	fireRunPerformed(pos);             // Say that we did the overall run using the position we stopped at.
       	    setState(DeviceState.READY);
        	
		} catch (ScanningException | InterruptedException i) {
			setState(DeviceState.FAULT);
			throw i;
		} catch (Exception ne) {
			setState(DeviceState.FAULT);
			throw new ScanningException(ne);
		}
	}

	
	public void reset() throws ScanningException {
		
		if (positioner instanceof LevelRunner) {
			((LevelRunner)positioner).reset();
		}
		runners.reset();
		writers.reset();

		super.reset();
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
			if (getModel().getDetectors()!=null) for (IRunnableDevice<?> device : getModel().getDetectors()) {
				if (device instanceof IPauseableDevice) ((IPauseableDevice)device).pause();
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
			if (getModel().getDetectors()!=null) for (IRunnableDevice<?> device : getModel().getDetectors()) {
				if (device instanceof IPauseableDevice) ((IPauseableDevice)device).resume();
			}
			paused.signalAll();
			
		} catch (ScanningException s) {
			throw s;
		} finally {
			lock.unlock();
		}
	}
}
