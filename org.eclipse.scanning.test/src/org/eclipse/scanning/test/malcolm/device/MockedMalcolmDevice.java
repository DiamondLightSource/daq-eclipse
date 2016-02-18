package org.eclipse.scanning.test.malcolm.device;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.LazyWriteableDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Random;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.DelegateNexusProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.event.MalcolmEventBean;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.malcolm.core.AbstractMalcolmDevice;

import uk.ac.diamond.malcolm.jacksonzeromq.connector.ZeromqConnectorService;

class MockedMalcolmDevice extends AbstractMalcolmDevice<Map<String, Object>> {
	
	private   static INexusFileFactory   factory;

	protected Map<String,Object> params;
	
	// Latch used such that one thread may wait the state changing at a time.
	// As of 04 Sep 2015 this is only used to latch external test threads whilst they wait for
	// a device state change. Unless there is a good reason not to, this map should probably be injected when
	// necessary (e.g from tests) if it is not part of the main device functionality. Code has been added
	// to support this. Moving to inject would allow the latch methods to be removed.
	private LatchDelegate latcher;

	// Only allows one thread to run a callableTask on the device at once
	private ReentrantLock  taskRunLock;

	private DeviceState state;
	

	/**
	 * Dummy task block to be overridden in subclasses
	 */
	protected Callable<Long> callableTask;

		
	MockedMalcolmDevice(String name) throws ScanningException {
		this(name, new LatchDelegate());
		callableTask = new Callable<Long>() {
			@Override
			public Long call() throws Exception {
				throw new MalcolmDeviceException("Method not implemented!");
			}
		};
	}
	
	MockedMalcolmDevice(String name, final LatchDelegate latcher) throws ScanningException {
		
		super(new ZeromqConnectorService()); // Hard coded, that's the way we role in tests.
		this.latcher = latcher;
		this.taskRunLock    = new ReentrantLock(true);
		setDeviceState(DeviceState.IDLE);
		setName(name);
	}

	public DeviceState getState() {
		return state;
	}

	protected void setState(DeviceState state, String message) throws MalcolmDeviceException {

		DeviceState old = this.state;
		this.state = state;

		latcher.setState(state);
	
		if (eventDelegate!=null && old!=state) {
			try {
				eventDelegate.sendStateChanged(state, old, message);
			} catch (Exception e) {
				throw new MalcolmDeviceException(this, "Internal error, cannot notify changing state to "+state, e);
			}
		}
	}

	@Override
	public void abort() throws ScanningException {
		
		if (!getState().isAbortable()) {
			throw new MalcolmDeviceException(this, "Device is in state "+getState()+" which cannot be aborted!");
		}
		
		setDeviceState(DeviceState.ABORTING); // Tells any running loops that we are killing it
		try {
			if (taskRunLock.tryLock() || taskRunLock.tryLock(5, TimeUnit.SECONDS)) {
				setDeviceState(DeviceState.ABORTED);
				taskRunLock.unlock();
			} else {
				// No sure what to do here as this lock is now no longer to do with pause
				setDeviceState(DeviceState.ABORTED);
			}
			
		} catch (InterruptedException e) {
			throw new MalcolmDeviceException(this, "Lock waiting for abort interupted!", e);
		}
	}

	@Override
	public Map<String, Object> validate(Map<String, Object> params) throws MalcolmDeviceException {
		if (!params.containsKey("shape")) throw new MalcolmDeviceException(this, "shape must be set!");
		if (!params.containsKey("nframes")) throw new MalcolmDeviceException(this, "nframes must be set!");
		if (!params.containsKey("file")) throw new MalcolmDeviceException(this, "file must be set!");
		if (!params.containsKey("exposure")) throw new MalcolmDeviceException(this, "exposure must be set!");
		return null;
	}

	@Override
	public void configure(Map<String, Object> params) throws ScanningException {
		
		validate(params);
		setDeviceState(DeviceState.CONFIGURING);
		this.params = params;
		if (params.containsKey("configureSleep")) {
			try {
				long sleepTime = Math.round(((double)params.get("configureSleep"))*1000d);
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				throw new MalcolmDeviceException(this, "Cannot sleep during configure!", e);
			}
		}
		setDeviceState(DeviceState.READY);
		
		// We configure a bean with all the scan specific things
		final MalcolmEventBean bean = new MalcolmEventBean();
		bean.setFilePath(params.get("file").toString());
		bean.setDatasetPath("/entry/data");
		bean.setDeviceName(getName());
		bean.setBeamline("Testing");
		bean.setPercentComplete(0d);
        setTemplateBean(bean);
	}

	@Override
	public void run(IPosition pos) throws ScanningException {
		
		if (!getState().isRunnable()) throw new MalcolmDeviceException("Malcolm is in non-runnable state "+getState());

		if (isLocked()) throw new MalcolmDeviceException(this, "Device '"+getName()+"' is already running or paused!");
		if (getState().isRunning()) throw new MalcolmDeviceException(this, "Device '"+getName()+"' is already running or paused!");
		
		try {
			run(params); // mimicks running malcolm hdf5
						
		} catch (Exception e) {
            setDeviceState(DeviceState.FAULT);
			throw new MalcolmDeviceException(this, "Cannot write", e);
			
		} finally {
			try {
				close();
			} catch (Exception e) {
				throw new MalcolmDeviceException(this, "Cannot cleanly close JMS session", e);
			}
		}
	}


	@Override
	public NexusObjectProvider<NXdetector> getNexusProvider(NexusScanInfo info) {
		return new DelegateNexusProvider<NXdetector>(getName(), NexusBaseClass.NX_DETECTOR, info, this);
	}

	@Override
	public NXdetector createNexusObject(NexusNodeFactory nodeFactory, NexusScanInfo info) {
		
		final NXdetector detector = nodeFactory.createNXdetector();
		// TODO FIXME Create links to HDF5 file here.
		return null;
	}


	/**
	 * Writes an HDF5 file with an image stack in.
	 * Does not need SWMR bindings because we are mocking the test in the same process.
	 * In the same process, the writing and multiple reading is allowed.
	 * 
	 * BODGED up scan loop to mimic writing for Mock test.
	 * 
	 * @param params
	 */
	protected void run(final Map<String, Object> params) throws Exception {

		setDeviceState(DeviceState.RUNNING); // Will send an event

        int amount = (int)params.get("nframes");
        
        // Send scan start
		sendEvent(new MalcolmEventBean(getState()));
     
        try {
    		NexusFile file=null;
    		try {
    			file = factory.newNexusFile(params.get("file").toString(), false);  // DO NOT COPY!
    			file.openToWrite(true); // DO NOT COPY!

    			GroupNode par = file.getGroup("/entry/data", true); // DO NOT COPY!
    			
				int[] ishape = (int[])params.get("shape");
				if (ishape==null) ishape = new int[]{1024,1024};

				final int[] shape = new int[]{1,  ishape[0], ishape[1]};
    			final int[] max   = new int[]{-1, ishape[0], ishape[1]};
    			ILazyWriteableDataset writer = new LazyWriteableDataset("image", Dataset.FLOAT, shape, max, shape, null); // DO NOT COPY!
    			file.createData(par, writer); 
				file.close();

    			int index = 0;
    			while(getState().isRunning()) {

    				try {
						acquireRunLock(); // Blocks if paused.
	
	    				int[] start = {index, 0, 0};
	    				int[] stop  = {index+1, 1024, 1024};
	    				index++;
	    				if (index>23) index = 23; // Stall on the last image to avoid writing massive stacks
	    				
	    				IDataset       rimage   = Random.rand(new int[]{1, ishape[0], ishape[1]});
	    				rimage.setName("image");
	   				    writer.setSlice(new IMonitor.Stub(), rimage, start, stop, null);
	   				    file.flush();
	   				    
	   					long exposure = Math.round(((double)params.get("exposure"))*1000d);
	   					Thread.sleep(exposure);
	    				System.out.println(">> HDF5 wrote image to "+params.get("file").toString());
	    				
	    				if (index>=amount) {
	    					break;
	    				}
    				} finally {
    					releaseRunLock();
     				}

    			}
   			
    		} catch (Exception ne) {
    			ne.printStackTrace();
    			
			}
			
			setDeviceState(DeviceState.READY); // State change
	        sendEvent(new MalcolmEventBean(getState())); // Scan end event

        
        } catch (Exception ne) {
        	ne.printStackTrace();
    		setState(DeviceState.FAULT, ne.getMessage());
     	    throw ne;
     	    
        } finally {
            try {
            	releaseRunLock();
            } catch (IllegalMonitorStateException ignored) {
            	// We try to make sure that the lock is released if the run thread has it.
            	// Since we are multi-threading here, it could be released
            	// by a resume call at any time. Therefore isLocked() is not
            	// reliable to protect the unlock()
            }
        }
	}
	
	@Override
	public DeviceState latch(long time, TimeUnit unit, DeviceState... ignoredStates) throws MalcolmDeviceException {
		try {
			return latcher.latch(this, time, unit, ignoredStates);
		} catch (Exception e) {
			throw new MalcolmDeviceException(this, e);
		}
	}
	
	public String getLockMessage() {
		final StringBuilder buf = new StringBuilder();
		buf.append("Hold Count=");
		buf.append(taskRunLock.getHoldCount());
		buf.append(" ; ");
		
		buf.append("Queue Length=");
		buf.append(taskRunLock.getQueueLength());
		buf.append(" ; ");

		buf.append("Has Queued=");
		buf.append(taskRunLock.hasQueuedThreads());
		buf.append(" ; ");
		return buf.toString();
	}
	
	public boolean isLocked() {
		return taskRunLock.isLocked();
	}


	/**
	 * Attempts to acquire the runLock with a timeout. MUST be used in conjunction with 
	 * {@link #releaseRunLock()} in this, or derived classes as below:
	 *
	 * <pre>
	 * <code>
	 * acquireRunLock();
	 * try {
	 *    doStuff();   // whatever your run operation is  
	 * }
	 * finally {
	 *    releaseRunLock();
	 * }
	 * </code>
	 * </pre>
	 * 
	 * @param scan
	 * @throws Exception
	 */
	protected boolean acquireRunLock() throws InterruptedException {
		
		return taskRunLock.tryLock() || taskRunLock.tryLock(2, TimeUnit.SECONDS);
	}
	
	/**
	 * Releases the runLock. MUST be used in conjunction with 
	 * {@link #acquireRunLock()} in this, or derived classes as below:
	 *
	 * <pre>
	 * <code>
	 * acquireRunLock();
	 * try {
	 *    doStuff();   // whatever your run operation is  
	 * }
	 * finally {
	 *    releaseRunLock();
	 * }
	 * </code>
	 * </pre>
	 * 
	 * @param scan
	 * @throws Exception
	 */
	protected void releaseRunLock() {
		taskRunLock.unlock();
	}
	
	/**
	 * Wrapper method to allow subclasses to enact the task block via the beforeExecute hook. 
	 * The task only proceeds if the device is in the RUNINNG state
	 * 
	 * @return	The return value of the underlying Callable if required
	 * @throws MalcolmDeviceException
	 */
	protected Long executeTask() throws MalcolmDeviceException {
		Long retObject = null;
		try {
			beforeExecute();
			if (getState().equals(DeviceState.RUNNING)) {		//Only proceed with the task if we're in the right state
				if (acquireRunLock()) {
					try {
						retObject = callableTask.call();						
					}
					finally {
						releaseRunLock();
					}
				}
				else {
					throw new MalcolmDeviceException(this, "Could not acquire the task run lock");					
				}
			}
			afterExecute();
		} 
		catch (Exception e) {
			throw new MalcolmDeviceException(this, "Could not execute the task", e );
		}
		return retObject;
	}

	@Override
	public void reset() throws MalcolmDeviceException {
		// TODO Auto-generated method stub
		
	}

}
