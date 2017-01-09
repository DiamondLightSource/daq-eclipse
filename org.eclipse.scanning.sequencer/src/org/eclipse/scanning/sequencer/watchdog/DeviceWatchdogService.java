package org.eclipse.scanning.sequencer.watchdog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IDeviceController;
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
	public IDeviceController create(IPausableDevice<?> device) {
		
		if (!Boolean.getBoolean("org.eclipse.scanning.watchdogs.active")) return null;
		if (templates==null) return null;
		try {
			DeviceController controller = new DeviceController(device);
			if (device instanceof AbstractRunnableDevice<?>) {
				controller.setBean(((AbstractRunnableDevice<?>)device).getBean());
			}
			List<IDeviceWatchdog> objects = new ArrayList<>(templates.size());
			for (final IDeviceWatchdog dog : templates) {
				IDeviceWatchdog ndog = dog.getClass().newInstance();
				ndog.setModel(dog.getModel());
				ndog.setController(controller);
				objects.add(ndog);
			}
			controller.setObjects(objects);
			return controller;
		} catch (Exception ne) {
			ne.printStackTrace();
			logger.error("Cannot create watchdogs", ne);
			return null;
		}
	}

}
