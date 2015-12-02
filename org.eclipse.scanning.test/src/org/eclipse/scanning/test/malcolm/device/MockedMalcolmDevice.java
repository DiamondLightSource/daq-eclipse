package org.eclipse.scanning.test.malcolm.device;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Random;
import org.eclipse.dawnsci.hdf5.HierarchicalDataFactory;
import org.eclipse.dawnsci.hdf5.IHierarchicalDataFile;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.State;
import org.eclipse.scanning.api.malcolm.event.MalcolmEventBean;
import org.eclipse.scanning.malcolm.core.AbstractMalcolmDevice;

import uk.ac.diamond.malcom.jacksonzeromq.connector.ZeromqConnectorService;

class MockedMalcolmDevice extends AbstractMalcolmDevice<Map<String, Object>> {
	
	protected Map<String,Object> params;
	
	// Latch used such that one thread may wait the state changing at a time.
	// As of 04 Sep 2015 this is only used to latch external test threads whilst they wait for
	// a device state change. Unless there is a good reason not to, this map should probably be injected when
	// necessary (e.g from tests) if it is not part of the main device functionality. Code has been added
	// to support this. Moving to inject would allow the latch methods to be removed.
	private LatchDelegate latcher;

	// Only allows one thread to run a callableTask on the device at once
	private ReentrantLock  taskRunLock;

	private State state;
	

	/**
	 * Dummy task block to be overridden in subclasses
	 */
	protected Callable<Long> callableTask;

		
	MockedMalcolmDevice(String name) throws MalcolmDeviceException {
		this(name, new LatchDelegate());
		callableTask = new Callable<Long>() {
			@Override
			public Long call() throws Exception {
				throw new MalcolmDeviceException("Method not implemented!");
			}
		};
	}
	
	MockedMalcolmDevice(String name, final LatchDelegate latcher) throws MalcolmDeviceException {
		
		super(new ZeromqConnectorService()); // Hard coded, that's the way we role in tests.
		this.latcher = latcher;
		this.taskRunLock    = new ReentrantLock(true);
		setState(State.IDLE);
		this.name = name;
	}

	public State getState() {
		return state;
	}
	
	protected void setState(State state) throws MalcolmDeviceException {
		this.setState(state, null);
	}

	protected void setState(State state, String message) throws MalcolmDeviceException {

		State old = this.state;
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
	public void abort() throws MalcolmDeviceException {
		
		if (!getState().isAbortable()) {
			throw new MalcolmDeviceException(this, "Device is in state "+getState()+" which cannot be aborted!");
		}
		
		setState(State.ABORTING); // Tells any running loops that we are killing it
		try {
			if (taskRunLock.tryLock() || taskRunLock.tryLock(5, TimeUnit.SECONDS)) {
				setState(State.ABORTED);
				taskRunLock.unlock();
			} else {
				// No sure what to do here as this lock is now no longer to do with pause
				setState(State.ABORTED);
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
	public void configure(Map<String, Object> params) throws MalcolmDeviceException {
		
		validate(params);
		setState(State.CONFIGURING);
		this.params = params;
		if (params.containsKey("configureSleep")) {
			try {
				long sleepTime = Math.round(((double)params.get("configureSleep"))*1000d);
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				throw new MalcolmDeviceException(this, "Cannot sleep during configure!", e);
			}
		}
		setState(State.READY);
		
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
	public void run() throws MalcolmDeviceException {
		
		if (!getState().isRunnable()) throw new MalcolmDeviceException("Malcolm is in non-runnable state "+getState());

		if (isLocked()) throw new MalcolmDeviceException(this, "Device '"+getName()+"' is already running or paused!");
		if (getState().isRunning()) throw new MalcolmDeviceException(this, "Device '"+getName()+"' is already running or paused!");
		
		try {
			write(params); // mimicks writing
						
		} catch (Exception e) {
            setState(State.FAULT);
			throw new MalcolmDeviceException(this, "Cannot write", e);
			
		} finally {
			try {
				close();
			} catch (Exception e) {
				throw new MalcolmDeviceException(this, "Cannot cleanly close JMS session", e);
			}
		}
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
	protected void write(final Map<String, Object> params) throws Exception {

		setState(State.RUNNING); // Will send an event

        int count  = 0;
        int amount = (int)params.get("nframes");
        
        // Send scan start
		sendEvent(new MalcolmEventBean(getState(), true));
     
        try {
           
			while(getState().isRunning()) {
	
				IHierarchicalDataFile file=null;
				try {
					// We don't use sleep for ms because this 
					// makes the system inherently slow, lock/unlock is immeditate.
					acquireRunLock(); // Blocks if paused.
						
					file = HierarchicalDataFactory.getWriter(params.get("file").toString());
	
					int[] shape = (int[])params.get("shape");
					if (shape==null) shape = new int[]{1024,1024};
					IDataset       rimage   = Random.rand(shape);
					rimage.setName("image");
	
					file.group("/entry");
					file.group("/entry/data");
					String path = file.appendDataset(rimage.getName(), rimage, "/entry/data");
					count++;
	
					// We mimic and event coming in from Malcolm
					// In reality these will come in from ZeroMQ but
					// will call sendEvent(...) in the same way.
					final MalcolmEventBean bean = new MalcolmEventBean(getState());
					bean.setPercentComplete((count/amount)*100d);				
					
					// Hardcoded shape change of dataset, in reality it will not be so simple.
					bean.setOldShape(new int[]{count-1, shape[0], shape[1]});
					bean.setNewShape(new int[]{count, shape[0], shape[1]});
					sendEvent(bean);
	
					System.out.println("Image "+count+" HDF5 written image to dataset "+path);
					
				} finally {
					releaseRunLock();
					if (file!=null) file.close();
				}
				
				// Break if done
				if (count>=amount) {
					break;
				}
				
				// Sleep (no need to lock while sleeping)
				long exposure = Math.round(((double)params.get("exposure"))*1000d);
				Thread.sleep(exposure);

			} // End fake scanning loop.
			
			setState(State.READY); // State change
	        sendEvent(new MalcolmEventBean(getState(), false, true)); // Scan end event

        
        } catch (Exception ne) {
        	ne.printStackTrace();
    		setState(State.FAULT, ne.getMessage());
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
	public State latch(long time, TimeUnit unit, State... ignoredStates) throws MalcolmDeviceException {
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
			if (getState().equals(State.RUNNING)) {		//Only proceed with the task if we're in the right state
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
