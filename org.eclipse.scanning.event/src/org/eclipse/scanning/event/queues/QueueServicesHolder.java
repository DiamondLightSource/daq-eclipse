package org.eclipse.scanning.event.queues;

import org.eclipse.scanning.api.device.IDeviceService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.queues.IQueueService;

/**
 * Class to hold services referenced in the implementation of the 
 * {@link IQueueService} and it's subsystems. Services are configured
 * by OSGi (or may be set by tests).
 * 
 * @author Michael Wharmby
 *
 */
public final class QueueServicesHolder {

	private static IDeviceService deviceService;
	private static IEventService eventService;
	private static IQueueService queueService;

	public static IDeviceService getDeviceService() {
		return deviceService;
	}

	public static void setDeviceService(IDeviceService deviceService) {
		QueueServicesHolder.deviceService = deviceService;
	}

	public static void unsetDeviceService() {
		QueueServicesHolder.deviceService = null;
	}

	public static IEventService getEventService() {
		return eventService;
	}

	public static void setEventService(IEventService eventService) {
		QueueServicesHolder.eventService = eventService;
	}

	public static void unsetEventService() {
		QueueServicesHolder.eventService = null;
	}

	public static IQueueService getQueueService() {
		return queueService;
	}

	public static void setQueueService(IQueueService queueService) {
		QueueServicesHolder.queueService = queueService;
	}

	public static void unsetQueueService() {
		QueueServicesHolder.queueService = null;
	}

}
