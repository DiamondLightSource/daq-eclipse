package org.eclipse.scanning.sequencer;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.annotation.AnnotationManager;
import org.eclipse.scanning.api.annotation.scan.PointEnd;
import org.eclipse.scanning.api.annotation.scan.PointStart;
import org.eclipse.scanning.api.annotation.scan.ScanAbort;
import org.eclipse.scanning.api.annotation.scan.ScanEnd;
import org.eclipse.scanning.api.annotation.scan.ScanFault;
import org.eclipse.scanning.api.annotation.scan.ScanFinally;
import org.eclipse.scanning.api.annotation.scan.ScanPause;
import org.eclipse.scanning.api.annotation.scan.ScanResume;
import org.eclipse.scanning.api.annotation.scan.ScanStart;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IPausableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IDeviceDependentIterable;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanInformation;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.sequencer.nexus.INexusScanFileManager;
import org.eclipse.scanning.sequencer.nexus.NexusScanFileManager;
import org.eclipse.scanning.sequencer.nexus.NexusScanFileManager.DummyNexusScanFileManager;

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
	private AnnotationManager                    manager;
	
	// the nexus file
	private INexusScanFileManager nexusScanFileManager = null;
	
	/*
	 * Concurrency design recommended by Keith Ralphs after investigating
	 * how to pause and resume a collection cycle using Reentrant locks.
	 * Design requires these three fields.
	 */
	private ReentrantLock    lock;
	private Condition        paused;
	private volatile boolean awaitPaused;
	
	/**
	 * Used for clients that would like to wait until the run. Most useful
	 * if the run was started with a start() call then more work is done, then
	 * a latch() will join with the start and return once it is finished.
	 * 
	 * If the start hangs, so will calling latch, there is no timeout.
	 * 
	 */
	private CountDownLatch latch;
		
	/**
	 * Package private constructor, devices are created by the service.
	 */
	AcquisitionDevice() {
		super();
		this.lock      = new ReentrantLock();
		this.paused    = lock.newCondition();
		setName("solstice_scan");
		setPrimaryScanDevice(true);
		setRole(DeviceRole.VIRTUAL);
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
		
		setDeviceState(DeviceState.CONFIGURING);
		setModel(model);
		setBean(model.getBean()!=null?model.getBean():new ScanBean());
		getBean().setPreviousStatus(getBean().getStatus());
		getBean().setStatus(Status.QUEUED);
		
		positioner = runnableDeviceService.createPositioner();
		if (model.getDetectors()!=null) {
			// Make sure all devices report the same scan id
			for (IRunnableDevice<?> device : model.getDetectors()) {
				if (device instanceof AbstractRunnableDevice<?>) {
					AbstractRunnableDevice<?> adevice = (AbstractRunnableDevice<?>)device;
					adevice.setBean(getBean());
					adevice.setPrimaryScanDevice(false);
				}
			}
		}
		
		// create the nexus file, if appropriate
		boolean writesNexus = model.getFilePath() != null && ServiceHolder.getFactory() != null;
		if (writesNexus) {
			nexusScanFileManager = new NexusScanFileManager(this);
		} else { //nothing wired, don't write a nexus file
			nexusScanFileManager = new DummyNexusScanFileManager();
		}
		nexusScanFileManager.configure(model);
		nexusScanFileManager.createNexusFile(true);
		
		if (model.getDetectors()!=null) {
			runners = new DeviceRunner(model.getDetectors());
			if (writesNexus) {
				writers = new DeviceWriter(model.getDetectors());
			} else {
				writers = LevelRunner.createEmptyRunner();
			}
		} else {
			runners = LevelRunner.createEmptyRunner();
			writers = LevelRunner.createEmptyRunner();
		}
		
		// Create the manager and populate it
		if (manager!=null) manager.dispose(); // It is allowed to configure more than once.
		manager = new AnnotationManager(SequencerActivator.getInstance());
		manager.addDevices(getScannables(model));
		if (model.getMonitors()!=null) manager.addDevices(model.getMonitors());
		manager.addDevices(model.getDetectors());
		
		setDeviceState(DeviceState.READY); // Notify 
	}


	@Override
	public void run(IPosition parent) throws ScanningException, InterruptedException {
		
		if (getDeviceState()!=DeviceState.READY) throw new ScanningException("The device '"+getName()+"' is not ready. It is in state "+getDeviceState());
		
		ScanModel model = getModel();
		if (model.getPositionIterable()==null) throw new ScanningException("The model must contain some points to scan!");
		
		boolean errorFound = false;
		IPosition pos = null;
		try {
			if (latch!=null) latch.countDown();
			this.latch = new CountDownLatch(1);
	        // TODO Should we validate the position iterator that all
	        // the positions are valid before running the scan?
	        // It was called limit checking in GDA.
	        // Sometimes logic is needed to implement collision avoidance
			
    		// Set the size and declare a count
    		final int size  = getSize(model.getPositionIterable());
    		int count = 0;

    		fireStart(size);    		

    		// We allow monitors which can block a position until a setpoint is
    		// reached or add an extra record to the NeXus file.
    		if (model.getMonitors()!=null) positioner.setMonitors(model.getMonitors());

    		// The scan loop
        	pos = null; // We want the last point when we are done so don't use foreach
        	boolean firedFirst = false;
	        for (IPosition position : model.getPositionIterable()) {
				
	        	pos = position;
	        	pos.setStepIndex(count);
	        	
	        	if (!firedFirst) {
	        		// Notify that we will do a run and provide the first position.
	        		manager.invoke(ScanStart.class, pos);
	            	fireRunWillPerform(pos);
	            	firedFirst = true;
	        	}
	        	
	        	// Check if we are paused, blocks until we are not
	        	boolean continueRunning = checkPaused();
	        	if (!continueRunning) return; // finally block performed 
	        	
	        	// Run to the position
        		manager.invoke(PointStart.class, pos);
	        	positioner.setPosition(pos);   // moveTo in GDA8
	        	
	        	writers.await();               // Wait for the previous write out to return, if any
	        	
        		nexusScanFileManager.flushNexusFile(); // flush the nexus file
	        	runners.run(pos);              // GDA8: collectData() / GDA9: run() for Malcolm
	        	writers.run(pos, false);       // Do not block on the readout, move to the next position immediately.
		        		        	
	        	// Send an event about where we are in the scan
        		manager.invoke(PointEnd.class, pos);
	        	positionComplete(pos, count, size);
	        	++count;
	        }
	        
	        // On the last iteration we must wait for the final readout.
        	writers.await();                   // Wait for the previous read out to return, if any
        	
		} catch (ScanningException | InterruptedException i) {
			errorFound=true;
			processException(i);
			throw i;
			
		} catch (Exception ne) {
			errorFound=true;
			processException(ne);
			throw new ScanningException(ne);
			
		} finally {
			close(errorFound, pos);
		}
	}
	
	private void close(boolean errorFound, IPosition last) throws ScanningException {
		try {
			try {
				positioner.close();
				runners.close();
				writers.close();
				
				nexusScanFileManager.scanFinished(); // writes scanFinished and closes nexus file
	        	
				// We should not fire the run performed until the nexus file is closed.
	        	// Tests wait for this step and reread the file.
	       	    fireRunPerformed(last);              // Say that we did the overall run using the position we stopped at.
			
			} finally {
	       	    try {
					manager.invoke(ScanFinally.class, last);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
					throw new ScanningException(e);
				}
	       	    
	    		// only fire end if finished normally
	    		if (!errorFound) fireEnd();
			}
			
		} finally {
			if (latch!=null) latch.countDown();
		}
	}

	@Override
	public void latch() throws InterruptedException {
		if (latch==null) return;
		latch.await();
	}

	private void processException(Exception ne) throws ScanningException {
		
		if (!getBean().getStatus().isFinal()) getBean().setStatus(Status.FAILED);
		getBean().setMessage(ne.getMessage());
		try {
			manager.invoke(ScanFault.class, ne);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
			throw new ScanningException(ne);
		}
		setDeviceState(DeviceState.FAULT);
	}

	private void fireEnd() throws ScanningException {
		
		// Setup the bean to sent
		getBean().setPreviousStatus(getBean().getStatus());
		getBean().setStatus(Status.COMPLETE);
		getBean().setPercentComplete(100);
		getBean().setMessage("Scan Complete");
		
		// Will send the state of the scan off.
		try {
			manager.invoke(ScanEnd.class);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
			throw new ScanningException(e);
		}
   	    setDeviceState(DeviceState.READY); // Fires!
				
	}

	private void fireStart(int size) throws Exception {
		
		ScanInformation info = new ScanInformation();
		info.setSize(size);
		info.setRank(getScanRank(getModel().getPositionIterable()));
		info.setScannableNames(getScannableNames(getModel().getPositionIterable()));
		info.setFilePath(getModel().getFilePath());
		manager.addContext(info);
		
		// Setup the bean to sent
		getBean().setSize(size);	        
		getBean().setPreviousStatus(getBean().getStatus());
		getBean().setStatus(Status.RUNNING);
		
		// Will send the state of the scan off.
		setDeviceState(DeviceState.RUNNING); // Fires!
		
		// Leave previous state as running now that we have notified of the start.
		getBean().setPreviousStatus(Status.RUNNING);
	}

	public void reset() throws ScanningException {
		
		if (positioner instanceof LevelRunner) {
			((LevelRunner)positioner).reset();
		}
		runners.reset();
		writers.reset();

		super.reset();
	}

	/**
	 * Blocks until not paused
	 * @return true if state has not been set to a rest one, i.e. we are still scanning.
	 * @throws Exception
	 */
	private boolean checkPaused() throws Exception {
		
		if (!getDeviceState().isRunning() && getDeviceState()!=DeviceState.READY) {
			if (getDeviceState().isRestState()) return false;
			throw new Exception("The scan state is "+getDeviceState());
		}

		// Check the locking using a condition
    	if(!lock.tryLock(1, TimeUnit.SECONDS)) {
    		throw new ScanningException(this, "Internal Error - Could not obtain lock to run device!");    		
    	}
    	try {
    		if (!getDeviceState().isRunning() && getDeviceState()!=DeviceState.READY) {
    			throw new Exception("The scan state is "+getDeviceState());
    		}
       	    if (awaitPaused) {
        		setDeviceState(DeviceState.PAUSED);
        		manager.invoke(ScanPause.class);
        		paused.await();
        		setDeviceState(DeviceState.RUNNING);
        		manager.invoke(ScanResume.class);
        	}
    	} finally {
    		lock.unlock();
    	}
    	return true;
	}

	// TODO Abort can be interpreted different ways. As 'skip' for short exposure experiments
	// it finishes the current frame, writes file and stops motors. For long exposure it might
	// need to stop the detector exposing further.
	
	// TODO Abort can stop everything, including detectors motors and file writing immediately.
	// Should the model define the behaviour of abort for a given detector? This would allow
	// abort to be configurable for different detectors.
	
	@Override
	public void abort() throws ScanningException {
		
		try {
			lock.lockInterruptibly();
		} catch (Exception ne) {
			throw new ScanningException(ne);
		}
		
		setDeviceState(DeviceState.ABORTING);
		try {
			awaitPaused = true;
			
			positioner.abort();
			writers.abort();
			runners.abort();
			
			if (getModel().getDetectors()!=null) for (IRunnableDevice<?> device : getModel().getDetectors()) {
				device.abort();
			}

			setDeviceState(DeviceState.ABORTED);
    		manager.invoke(ScanAbort.class);
			
		} catch (ScanningException s) {
			throw s;
		} catch (Exception ne) {
			throw new ScanningException(ne);
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public void disable() throws ScanningException {
		// TODO Matt Gerring to implement this: call abort on all children
	}

	@Override
	public void pause() throws ScanningException {
		
		if (getDeviceState() != DeviceState.RUNNING) {
			throw new ScanningException(this, getName()+" is not running and cannot be paused!");
		}
		try {
			lock.lockInterruptibly();
		} catch (Exception ne) {
			throw new ScanningException(ne);
		}
		
		setDeviceState(DeviceState.SEEKING);
		try {
			awaitPaused = true;
			if (getModel().getDetectors()!=null) for (IRunnableDevice<?> device : getModel().getDetectors()) {
				if (device instanceof IPausableDevice) ((IPausableDevice)device).pause();
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
		
		if (getDeviceState() != DeviceState.PAUSED) {
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
				if (device instanceof IPausableDevice) ((IPausableDevice)device).resume();
			}
			paused.signalAll();
			
		} catch (ScanningException s) {
			throw s;
		} finally {
			lock.unlock();
		}
	}

	private int getSize(Iterable<IPosition> gen) throws GeneratorException {
		
		int size=0;
		if (gen instanceof IDeviceDependentIterable) {
			size = ((IDeviceDependentIterable)gen).size();
			
		} else if (gen instanceof IPointGenerator<?>) {
			size = ((IPointGenerator<?>)gen).size();
			
		} else {
		    for (IPosition unused : model.getPositionIterable()) size++; // Fast even for large stuff providing they do not check hardware on the next() call.
		}
		return size;   		
	}
	

	private Collection<IScannable<?>> getScannables(ScanModel model) throws ScanningException {
		final Collection<String>   names = getScannableNames(model.getPositionIterable());
		final Collection<IScannable<?>> ret = new ArrayList<>();
		for (String name : names) ret.add(runnableDeviceService.getDeviceConnectorService().getScannable(name));
		return ret;
	}
	
	private Collection<String> getScannableNames(Iterable<IPosition> gen) {
		
		Collection<String> names = null;
		if (gen instanceof IDeviceDependentIterable) {
			names = ((IDeviceDependentIterable)gen).getScannableNames();
			
		}
		if (names==null) {
			names = model.getPositionIterable().iterator().next().getNames();
		}
		return names;   		
	}
	
	private int getScanRank(Iterable<IPosition> gen) {
		int scanRank = -1;
		if (gen instanceof IDeviceDependentIterable) {
			scanRank = ((IDeviceDependentIterable)gen).getScanRank();
			
		}
		if (scanRank < 0) {
			scanRank = model.getPositionIterable().iterator().next().getScanRank();
		}
		if (scanRank < 0) {
			scanRank = 1;
		}
		
		return scanRank;   		
	}

}
