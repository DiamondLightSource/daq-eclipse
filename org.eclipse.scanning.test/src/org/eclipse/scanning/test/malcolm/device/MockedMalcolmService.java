package org.eclipse.scanning.test.malcolm.device;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.IMalcolmService;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;

public class MockedMalcolmService implements IMalcolmService {
	
	private Map<String, IMalcolmDevice> devices;
	private final LatchDelegate latcher;
	private boolean usePausableDevices;

	public MockedMalcolmService(final boolean pausable) {
		super();
		this.latcher = new LatchDelegate();
		usePausableDevices = pausable;
	}

	public void dispose() throws MalcolmDeviceException {
	}
	
	@Override
	public <T> IMalcolmDevice<T> getDevice(String name) throws MalcolmDeviceException {
		return getDevice(name, null);
	}
	@Override
	public <T> IMalcolmDevice<T> getDevice(String name, IPublisher<ScanBean> publisher) throws MalcolmDeviceException {
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
}
