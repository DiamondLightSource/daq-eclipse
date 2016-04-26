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
public final class ServiceHolder {

	private static IDeviceService deviceService;
	private static IEventService eventService;
	private static IQueueService queueService;

	public static IDeviceService getDeviceService() {
		return deviceService;
	}

	public static void setDeviceService(IDeviceService deviceService) {
		ServiceHolder.deviceService = deviceService;
	}

	public static void unsetDeviceService() {
		ServiceHolder.deviceService = null;
	}

	public static IEventService getEventService() {
		return eventService;
	}

	public static void setEventService(IEventService eventService) {
		ServiceHolder.eventService = eventService;
	}

	public static void unsetEventService() {
		ServiceHolder.eventService = null;
	}

	public static IQueueService getQueueService() {
		return queueService;
	}

	public static void setQueueService(IQueueService queueService) {
		ServiceHolder.queueService = queueService;
	}

	public static void unsetQueueService() {
		ServiceHolder.queueService = null;
	}

}
