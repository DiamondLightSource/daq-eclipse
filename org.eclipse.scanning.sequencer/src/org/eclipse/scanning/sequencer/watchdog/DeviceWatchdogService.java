package org.eclipse.scanning.sequencer.watchdog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.scanning.api.device.IDeviceWatchdog;
import org.eclipse.scanning.api.device.IDeviceWatchdogService;
import org.eclipse.scanning.api.device.IPausableDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceWatchdogService implements IDeviceWatchdogService {
	
	private static Logger logger = LoggerFactory.getLogger(DeviceWatchdogService.class);
	private List<IDeviceWatchdog> templates = Collections.synchronizedList(new ArrayList<>(3));
	
	static {
		if (System.getProperty("org.eclipse.scanning.watchdogs.active")==null) {
			System.setProperty("org.eclipse.scanning.watchdogs.active", "true");
		}
	}

	@Override
	public void register(IDeviceWatchdog dog) {
		templates.add(dog);
	}

	@Override
	public void unregister(IDeviceWatchdog dog) {
		templates.remove(dog);
	}

	@Override
	public List<IDeviceWatchdog> create(IPausableDevice<?> device) {
		
		if (!Boolean.getBoolean("org.eclipse.scanning.watchdogs.active")) return null;
		if (templates==null) return null;
		try {
			List<IDeviceWatchdog> ret = new ArrayList<>(templates.size());
			for (final IDeviceWatchdog dog : templates) {
				IDeviceWatchdog ndog = dog.getClass().newInstance();
				ndog.setModel(dog.getModel());
				ndog.setDevice(device);
				ret.add(ndog);
			}
			return ret;
		} catch (Exception ne) {
			ne.printStackTrace();
			logger.error("Cannot create watchdogs", ne);
			return null;
		}
	}

}
