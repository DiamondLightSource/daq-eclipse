package org.eclipse.scanning.event.ui;

import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.points.IPointGeneratorService;

public class ServiceHolder {

	private static IEventConnectorService eventConnectorService;
	private static IEventService          eventService;
	private static IPointGeneratorService generatorService;

	public static IEventConnectorService getEventConnectorService() {
		return eventConnectorService;
	}

	public static void setEventConnectorService(IEventConnectorService eventService) {
		ServiceHolder.eventConnectorService = eventService;
	}

	public static IEventService getEventService() {
		return eventService;
	}

	public static void setEventService(IEventService eventService) {
		ServiceHolder.eventService = eventService;
	}

	public static IPointGeneratorService getGeneratorService() {
		return generatorService;
	}

	public static void setGeneratorService(IPointGeneratorService generatorService) {
		ServiceHolder.generatorService = generatorService;
	}
}
