package org.eclipse.scanning.sequencer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.builder.NexusScanFile;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IDeviceConnectorService;
import org.eclipse.scanning.api.device.IPausableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IDeviceDependentIterable;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.sequencer.nexus.NexusScanFileBuilder;
import org.eclipse.scanning.sequencer.nexus.ScanPointsWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private static final Logger logger = LoggerFactory.getLogger(AcquisitionDevice.class);

	// Scanning stuff
	private IPositioner                          positioner;
	private LevelRunner<IRunnableDevice<?>>      runners;
	private LevelRunner<IRunnableDevice<?>>      writers;
	
	// the nexus file
	private NexusScanFile nexusScanFile = null;
	
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
		setName("solstice_scan");
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
					((AbstractRunnableDevice<?>)device).setBean(getBean());
				}
			}
			runners = new DeviceRunner(model.getDetectors());
			writers = new DeviceWriter(model.getDetectors());
		} else {
			runners = LevelRunner.createEmptyRunner();
			writers = LevelRunner.createEmptyRunner();
		}
		
		// add legacy metadata scannables and 
		// tell each scannable whether or not it is a metadata scannable in this scan
		Collection<String> scannableNames = getScannableNames(model.getPositionIterable());
		setMetadataScannables(model, scannableNames);
		
		// create the nexus file, if appropriate
		try {
			int scanRank = getScanRank(model.getPositionIterable());
			createNexusFile(model, scannableNames, scanRank);
		} catch (NexusException e) {
			throw new ScanningException(e);
		}
		
		setDeviceState(DeviceState.READY); // Notify 
	}

	/**
	 * Augments the set of metadata scannables in the model with: <ul>
	 * <li>any scannables from the legacy spring configuration;</li>
	 * <li>the required scannables of any scannables in the scan;</li>
	 * </ul> 
	 * @param model
	 * @throws ScanningException
	 */
	@SuppressWarnings("deprecation")
	private void setMetadataScannables(ScanModel model, Collection<String> scannableNames) throws ScanningException {
		// TODO: does this belong in NexusScanFileBuilder? It's clogging up this class
		// and only NexusScanFileBuilder needs to know about metadata scannables
		
		// build up the set of all metadata scannables
		Set<String> metadataScannableNames = new HashSet<>();
		
		// add the metadata scannables in the model
		metadataScannableNames.addAll(model.getMetadataScannables().stream().
				map(m -> m.getName()).collect(Collectors.toSet()));
		
		// add the global metadata scannables, and the required metadata scannables for
		// each scannable in the scan
		final IDeviceConnectorService connectorService = getConnectorService();
		metadataScannableNames.addAll(connectorService.getGlobalMetadataScannableNames());
		
		// the set of scannable names to check for dependencies
		Set<String> scannableNamesToCheck = new HashSet<>();
		scannableNamesToCheck.addAll(metadataScannableNames);
		scannableNamesToCheck.addAll(scannableNames);
		do {
			// check the given set of scannable names for dependencies
			// each iteration checks the scannable names added in the previous one
			Set<String> requiredScannables = scannableNamesToCheck.stream().flatMap(
					name -> connectorService.getRequiredMetadataScannableNames(name).stream())
					.filter(name -> !metadataScannableNames.contains(name))
					.collect(Collectors.toSet());
			
			metadataScannableNames.addAll(requiredScannables);
			scannableNamesToCheck = requiredScannables;
		} while (!scannableNamesToCheck.isEmpty());
		
		// remove any scannable names in the scan from the list of metadata scannables
		metadataScannableNames.removeAll(scannableNames);
		
		// get the metadata scannables for the given names
		IDeviceConnectorService deviceConnectorService = getConnectorService();
		List<IScannable<?>> metadataScannables = new ArrayList<>(metadataScannableNames.size());
		for (String scannableName : metadataScannableNames) {
			IScannable<?> metadataScannable = deviceConnectorService.getScannable(scannableName);
			metadataScannables.add(metadataScannable);
		}
		
		model.setMetadataScannables(metadataScannables);
	}

	/**
	 * Creates the NeXus file for the scan, if the scan is configured to
	 * create one.
	 * 
	 * @param model scan model
	 * @return <code>true</code> if a nexus file was successfully created, <code>false</code> otherwise
	 * @throws NexusException if a nexus file should be created
	 * @throws ScanningException 
	 */
	private boolean createNexusFile(ScanModel model, Collection<String> scannableNames, int scanRank) throws NexusException, ScanningException {
		
		if (model.getFilePath() == null || ServiceHolder.getFactory() == null) {
			return false; // nothing wired, don't write a nexus file 
		}
		
		NexusScanFileBuilder fileBuilder = new NexusScanFileBuilder(getConnectorService());
		nexusScanFile = fileBuilder.createNexusFile(model, scannableNames, scanRank);
		
		// TODO FIXME MD Does this not cause a memory leak as run listeners are added
		// but never removed?
		ScanPointsWriter scanPointsWriter = fileBuilder.getScanPointsWriter();
    	positioner.addPositionListener(scanPointsWriter);
    	addRunListener(scanPointsWriter);
		
		nexusScanFile.openToWrite();
		
		return true; // successfully created file
	}

	@Override
	public void run(IPosition parent) throws ScanningException, InterruptedException {
		
		if (getDeviceState()!=DeviceState.READY) throw new ScanningException("The device '"+getName()+"' is not ready. It is in state "+getDeviceState());
		
		ScanModel model = getModel();
		if (model.getPositionIterable()==null) throw new ScanningException("The model must contain some points to scan!");
		
		IPosition pos = null;
		try {
	        // TODO Should we validate the position iterator that all
	        // the positions are valid before running the scan?
	        // It was called limit checking in GDA.
	        // Sometimes logic is needed to implement collision avoidance
			
    		// Set the size and declare a count
    		int size  = getSize(model.getPositionIterable());
    		int count = 0;

    		fireStart(size);    		


    		// We allow monitors which can block a position until a setpoint is
    		// reached or add an extra record to the NeXus file.
    		if (model.getMonitors()!=null) positioner.setMonitors(model.getMonitors());

    		// The scan loop
        	pos = null; // We want the last point when we are done so don't use foreach
        	boolean firedFirst = false;
	        for (Iterator<IPosition> it = model.getPositionIterable().iterator(); it.hasNext();) {
				
	        	pos = it.next();
	        	pos.setStepIndex(count);
	        	
	        	if (!firedFirst) {
	        		// Notify that we will do a run and provide the first position.
	            	fireRunWillPerform(pos);
	            	firedFirst = true;
	        	}
	        	
	        	// Check if we are paused, blocks until we are not
	        	boolean continueRunning = checkPaused();
	        	if (!continueRunning) {
	        		return; // finally block performed 
	        	}
	        	
	        	// TODO Some validation on each point
	        	// perhaps replacing atPointStart(..)
	        	// Whether to deal with atLineStart() and atPointStart()
	        	
	        	// Run to the position
	        	positioner.setPosition(pos);   // moveTo in GDA8
	        	
	        	writers.await();               // Wait for the previous read out to return, if any
	        	if (nexusScanFile!=null) {
	        		int code = nexusScanFile.flush();         // flush the nexus file
	        		if (code<0) logger.warn("Problem flushing during scan! Flush code is "+code);
	        	}
	        	runners.run(pos);              // GDA8: collectData() / GDA9: run() for Malcolm
	        	writers.run(pos, false);       // Do not block on the readout, move to the next position immediately.
		        		        	
	        	// Send an event about where we are in the scan
	        	positionComplete(pos, count, size);
	        	++count;
	        }
	        
	        // On the last iteration we must wait for the final readout.
        	writers.await();                   // Wait for the previous read out to return, if any
        	
		} catch (ScanningException | InterruptedException i) {
			if (!getBean().getStatus().isFinal()) getBean().setStatus(Status.FAILED);
			getBean().setMessage(i.getMessage());
			setDeviceState(DeviceState.FAULT);
			throw i;
			
		} catch (Exception ne) {
			if (!getBean().getStatus().isFinal()) getBean().setStatus(Status.FAILED);
			getBean().setMessage(ne.getMessage());
			setDeviceState(DeviceState.FAULT);
			throw new ScanningException(ne);
		} finally {
        	if (nexusScanFile!=null) {
				try {
					nexusScanFile.close();
				} catch (NexusException e) {
					throw new ScanningException("Could not close nexus file", e);
				}
        	}
        	// We should not fire the run performed until the nexus file is closed.
        	// Tests wait for this step and reread the file.
       	    fireRunPerformed(pos);             // Say that we did the overall run using the position we stopped at.
		}
		// only fire end if finished normally
		fireEnd();
	}

	private void fireEnd() throws ScanningException {
		
		// Setup the bean to sent
		getBean().setPreviousStatus(getBean().getStatus());
		getBean().setStatus(Status.COMPLETE);
		getBean().setPercentComplete(100);
		
		// Will send the state of the scan off.
   	    setDeviceState(DeviceState.READY); // Fires!
				
	}

	private void fireStart(int size) throws ScanningException {
		
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
        		paused.await();
        		setDeviceState(DeviceState.RUNNING);
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
			
		} catch (ScanningException s) {
			throw s;
		} catch (Exception ne) {
			throw new ScanningException(ne);
		} finally {
			lock.unlock();
		}
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
		
		setDeviceState(DeviceState.PAUSING);
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
	
	@Override
	public boolean isVirtual() {
		return true;
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
		if (scanRank<0) scanRank = 1;
		return scanRank;   		
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

}
