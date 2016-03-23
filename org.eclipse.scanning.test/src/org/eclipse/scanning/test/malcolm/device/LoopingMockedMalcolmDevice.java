package org.eclipse.scanning.test.malcolm.device;

import java.util.Map;

import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.event.MalcolmEventBean;
import org.eclipse.scanning.api.malcolm.models.MalcolmDetectorModelWithMap;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;

/**
 * Example Device that executes a callable task a number of times as defined by the params
 * @author fri44821
 *
 */
public class LoopingMockedMalcolmDevice extends PausableMockedMalcolmDevice {
	
	protected int count;
	protected int amount;
	
	public LoopingMockedMalcolmDevice(final String name,  final LatchDelegate latcher) throws ScanningException {
		super(name, latcher);
		setState(DeviceState.IDLE);
		setName(name);
	}

	@Override
	public MalcolmDetectorModelWithMap validate(MalcolmDetectorModelWithMap params) throws MalcolmDeviceException {
		throw new MalcolmDeviceException(this, "Validate is not implemented!");
	}

	@Override
	public void run(IPosition pos) throws MalcolmDeviceException {
		if (!getState().isRunnable()) {
			throw new MalcolmDeviceException("Malcolm is in non-runnable state "+getState());
		}

		if (getState().isRunning()) {
			throw new MalcolmDeviceException(this, "Device '"+getName()+"' is already running or paused!");
		}
		
		try {
			setState(DeviceState.RUNNING); // Will send an event

	        count  = 0;
	        amount = (int)model.getParameterMap().get("nframes");
	        
	        // Send scan start
			sendEvent(new MalcolmEventBean(getState()));
	           
			while(getState().isRunning()) {
				
				executeTask();
				// Break if done
				if (count>=amount) {
					break;
				}
				
				// Sleep (no need to lock while sleeping)
				long sleep = Math.round((double)model.getParameterMap().get("exposure")*1000d);
				Thread.sleep(sleep);

			} // End fake scanning loop.
			
			setState(DeviceState.IDLE); // State change
	        sendEvent(new MalcolmEventBean(getState())); // Scan end event        
        } 
		catch (Exception ne) {
			ne.printStackTrace();
    		setState(DeviceState.FAULT, ne.getMessage());
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
