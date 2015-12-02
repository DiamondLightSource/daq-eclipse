package org.eclipse.scanning.test.malcolm.device;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.eclipse.scanning.api.malcolm.IMalcolmConnection;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;

public class MockedConnection implements IMalcolmConnection {
	
	private Map<String, IMalcolmDevice> devices;
	private final LatchDelegate latcher;
	private boolean usePausableDevices;
	

	public MockedConnection(LatchDelegate latcher, final boolean pausable) {
		super();
		this.latcher       = latcher;
		usePausableDevices = pausable;
	}

	@Override
	public List<String> getDeviceNames() throws MalcolmDeviceException {
  	    return Arrays.asList("zebra");
	}

	@Override
	public IMalcolmDevice getDevice(String name) throws MalcolmDeviceException {
		try {
			if (devices==null || devices.isEmpty()) {
				devices = new HashMap<String, IMalcolmDevice>(1);
				IMalcolmDevice device = usePausableDevices ? 
				 		new MockedWriteInLoopPausableMalcolmDevice("zebra", latcher) : 
						new MockedMalcolmDevice("zebra");
				
				devices.put("zebra", device);
			}
			IMalcolmDevice device = devices.get(name);
			if (device!=null) return device;
			throw new MalcolmDeviceException("Invalid name "+name);
		    
		} catch (Exception ne) {
			throw new MalcolmDeviceException(null, "Unable to connect to device!", ne);
		}
	}

	@Override
	public void dispose() throws MalcolmDeviceException {
		if (devices==null) return;
		for (IMalcolmDevice device : devices.values()) device.dispose();
		devices.clear();
		devices = null;
	}

}
