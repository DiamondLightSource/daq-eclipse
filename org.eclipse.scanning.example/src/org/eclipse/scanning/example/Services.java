package org.eclipse.scanning.example;

import org.eclipse.scanning.api.event.IEventService;

public class Services {

	private static IEventService eventService;

	public static IEventService getEventService() {
		return eventService;
	}

	public static void setEventService(IEventService eventService) {
		Services.eventService = eventService;
	}
	
}
