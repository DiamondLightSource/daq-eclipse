package org.eclipse.scanning.event.queues;

import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.queues.IQueueControllerService;
import org.eclipse.scanning.api.event.queues.IQueueService;

/**
 * Class to hold services referenced in the implementation of the 
 * {@link IQueueService} and it's subsystems. Services are configured
 * by OSGi (or may be set by tests).
 * 
 * @author Michael Wharmby
 *
 */
public final class ServicesHolder {

	private static IRunnableDeviceService deviceService;
	private static IEventService eventService;
	private static IQueueService queueService;
	private static IQueueControllerService controllerService;

	public static IRunnableDeviceService getDeviceService() {
		return deviceService;
	}

	public static void setDeviceService(IRunnableDeviceService deviceService) {
		ServicesHolder.deviceService = deviceService;
	}

	public static void unsetDeviceService(IRunnableDeviceService deviceService) {
		if (ServicesHolder.deviceService == deviceService) {
			ServicesHolder.deviceService = null;
		}
	}

	public static IEventService getEventService() {
		return eventService;
	}

	public static void setEventService(IEventService eventService) {
		ServicesHolder.eventService = eventService;
	}

	public static void unsetEventService(IEventService eventService) {
		if (ServicesHolder.eventService == eventService) {
			ServicesHolder.eventService = null;
		}
	}

	public static IQueueService getQueueService() {
		return queueService;
	}

	public static void setQueueService(IQueueService queueService) {
		ServicesHolder.queueService = queueService;
	}

	public static void unsetQueueService(IQueueService queueService) {
		if (ServicesHolder.queueService == queueService) {
			ServicesHolder.queueService = null;
		}
	}
	
	public static void setQueueControllerService(IQueueControllerService controllerService) {
		ServicesHolder.controllerService = controllerService;
	}
	
	public static void unsetQueueControllerService(IQueueControllerService controllerService) {
		if (ServicesHolder.controllerService == controllerService) {
			ServicesHolder.controllerService = null;
		}
	}

}
