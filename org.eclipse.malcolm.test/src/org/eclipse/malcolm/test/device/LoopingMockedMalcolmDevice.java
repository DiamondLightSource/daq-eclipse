package org.eclipse.malcolm.test.device;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.eclipse.malcolm.api.MalcolmDeviceException;
import org.eclipse.malcolm.api.State;
import org.eclipse.malcolm.api.event.MalcolmEventBean;

/**
 * Example Device that executes a callable task a number of times as defined by the params
 * @author fri44821
 *
 */
public class LoopingMockedMalcolmDevice extends PausableMockedMalcolmDevice {
	
	protected int count;
	protected int amount;
	
	public LoopingMockedMalcolmDevice(final String name,  final LatchDelegate latcher) throws MalcolmDeviceException {
		super(name, latcher);
		setState(State.IDLE);
		this.name = name;
	}

	@Override
	public Map<String, Object> validate(Map<String, Object> params) throws MalcolmDeviceException {
		throw new MalcolmDeviceException(this, "Validate is not implemented!");
	}

	@Override
	public void run() throws MalcolmDeviceException {
		if (!getState().isRunnable()) {
			throw new MalcolmDeviceException("Malcolm is in non-runnable state "+getState());
		}

		if (getState().isRunning()) {
			throw new MalcolmDeviceException(this, "Device '"+getName()+"' is already running or paused!");
		}
		
		try {
			setState(State.RUNNING); // Will send an event

	        count  = 0;
	        amount = (int)params.get("nframes");
	        
	        // Send scan start
			sendEvent(new MalcolmEventBean(getState(), true));
	           
			while(getState().isRunning()) {
				
				executeTask();
				// Break if done
				if (count>=amount) {
					break;
				}
				
				// Sleep (no need to lock while sleeping)
				long sleep = Math.round((double)params.get("exposure")*1000d);
				Thread.sleep(sleep);

			} // End fake scanning loop.
			
			setState(State.IDLE); // State change
	        sendEvent(new MalcolmEventBean(getState(), false, true)); // Scan end event        
        } 
		catch (Exception ne) {
			ne.printStackTrace();
    		setState(State.FAULT, ne.getMessage());
     	    throw new MalcolmDeviceException(this, ne.getMessage(), ne);     	    
        } 
		finally {
			try {
				close();
			} 
			catch (Exception e) {
				throw new MalcolmDeviceException(this, "Cannot cleanly close JMS session", e);
			}
		}
	}
}
