package org.eclipse.scanning.sequencer;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.builder.NexusEntryBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusFileBuilder;
import org.eclipse.scanning.api.IScannable;
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
	private IPositioner      positioner;
	private DetectorRunner   detectors;
	private DetectorWriter   writers;
	
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
		
		setModel(model);
		setBean(model.getBean()!=null?model.getBean():new ScanBean());
		
		positioner = scanningService.createPositioner(deviceService);
		detectors = new DetectorRunner(model.getDetectors());
		writers   = new DetectorWriter(model.getDetectors());
		
		try {
			wireNexus(model);
		} catch (NexusException e) {
			throw new ScanningException(e);
		}
		
		setState(DeviceState.READY);
	}

	private boolean wireNexus(ScanModel model) throws NexusException, ScanningException {
		
		if (model.getFilePath()==null || ServiceHolder.getFactory()==null) return false; // nothing wired 
			
		// We use the new nexus framework to join everything up into the scan
		// Create a builder
		final NexusFileBuilder  fbuilder = ServiceHolder.getFactory().newNexusFileBuilder(model.getFilePath());
		final NexusEntryBuilder builder  = fbuilder.newEntry();
		builder.addDefaultGroups();
		
		// Add any devices we can get from the scan.
		final List<String> names = model.getPositionIterator().iterator().next().getNames();
		if (names!=null) for (String name : names) {
			IScannable<?> scannable = getDeviceService().getScannable(name);
			if (scannable instanceof INexusDevice) {
				builder.add(((INexusDevice)scannable).getNexusProvider());
			}
		}

		if (model.getDetectors()!=null) for (IRunnableDevice<?> detector : model.getDetectors()) {
			if (detector instanceof INexusDevice) {
				builder.add(((INexusDevice)detector).getNexusProvider());
			}
		}
		if (model.getMonitors()!=null) for (IScannable<?> scannable : model.getMonitors()) {
			if (scannable instanceof INexusDevice) {
				builder.add(((INexusDevice)scannable).getNexusProvider());
			}
		}
		
		fbuilder.saveFile();
		
		// Something got created
		return true;
	}

	@Override
	public void run(IPosition parent) throws ScanningException, InterruptedException {
		
		if (getState()!=DeviceState.READY) throw new ScanningException("The device '"+getName()+"' is not ready. It is in state "+getState());
		
		ScanModel model = getModel();
		if (model.getPositionIterator()==null) throw new ScanningException("The model must contain some points to scan!");
		
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
    		for (IPosition unused : model.getPositionIterator()) size++; // Fast even for large stuff

    		// The scan loop
	        for (IPosition pos : model.getPositionIterator()) {
	        	
	        	pos.setStepIndex(count);
	        	
	        	// Check if we are paused, blocks until we are not
	        	checkPaused();
	        	fireRunWillPerform(pos);
	        	
	        	// TODO Some validation on each point
	        	// perhaps replacing atPointStart(..)
	        	// Whether to deal with atLineStart() and atPointStart()
	        	
	        	// Run to the position
	        	positioner.setPosition(pos);   // moveTo in GDA8
	        	
	        	// check that beam is up. In the past this has been done with 
	        	// scannables at a given level that block until they are happy.
	        	
	        	writers.await();               // Wait for the previous read out to return, if any
	        	detectors.run(pos);            // GDA8: collectData() / GDA9: run() for Malcolm
	        	writers.run(pos, false);       // Do not block on the readout, move to the next position immediately.
		        		        	
	        	// Send an event about where we are in the scan
	        	fireRunPerformed(pos);
	        	positionComplete(pos, count+1, size);
	        	++count;
	        	
	        }
	        
	        // On the last iteration we must wait for the final readout.
        	writers.await();                   // Wait for the previous read out to return, if any
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
		detectors.reset();
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
			for (IRunnableDevice<?> device : getModel().getDetectors()) {
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
			for (IRunnableDevice<?> device : getModel().getDetectors()) {
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
