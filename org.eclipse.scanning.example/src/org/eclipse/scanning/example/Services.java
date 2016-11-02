package org.eclipse.scanning.example;

import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.IEventService;

public class Services {

	private static IEventService eventService;
	private static IRunnableDeviceService runnableDeviceService;

	public static IRunnableDeviceService getRunnableDeviceService() {
		return runnableDeviceService;
	}

	public static void setRunnableDeviceService(IRunnableDeviceService runnableDeviceService) {
		Services.runnableDeviceService = runnableDeviceService;
	}

	public static IEventService getEventService() {
		return eventService;
	}

	public static void setEventService(IEventService eventService) {
		Services.eventService = eventService;
	}

}
