package org.eclipse.scanning.event.queues;

import org.eclipse.scanning.api.device.IRunnableDeviceService;
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

	private static IRunnableDeviceService deviceService;
	private static IEventService eventService;
	private static IQueueService queueService;

	public static IRunnableDeviceService getDeviceService() {
		return deviceService;
	}

	public static void setDeviceService(IRunnableDeviceService deviceService) {
		QueueServicesHolder.deviceService = deviceService;
	}

	public static void unsetDeviceService(IRunnableDeviceService deviceService) {
		if (QueueServicesHolder.deviceService == deviceService) {
			QueueServicesHolder.deviceService = null;
		}
	}

	public static IEventService getEventService() {
		return eventService;
	}

	public static void setEventService(IEventService eventService) {
		QueueServicesHolder.eventService = eventService;
	}

	public static void unsetEventService(IEventService eventService) {
		if (QueueServicesHolder.eventService == eventService) {
			QueueServicesHolder.eventService = null;
		}
	}

	public static IQueueService getQueueService() {
		return queueService;
	}

	public static void setQueueService(IQueueService queueService) {
		QueueServicesHolder.queueService = queueService;
	}

	public static void unsetQueueService(IQueueService queueService) {
		if (QueueServicesHolder.queueService == queueService) {
			QueueServicesHolder.queueService = null;
		}
	}

}
