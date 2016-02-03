package org.eclipse.scanning.server.servlet;

import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.malcolm.IMalcolmService;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.scan.IDeviceConnectorService;
import org.eclipse.scanning.api.scan.IScanningService;

public class Services {

	private static IEventService           eventService;
	private static IPointGeneratorService  generatorService;
	private static IScanningService        scanService;
	private static IDeviceConnectorService connector;
	private static IMalcolmService         malcService;

	public static IEventService getEventService() {
		return eventService;
	}


	public static void setEventService(IEventService eventService) {
		Services.eventService = eventService;
	}


	public static IPointGeneratorService getGeneratorService() {
		return generatorService;
	}


	public static void setGeneratorService(IPointGeneratorService generatorService) {
		Services.generatorService = generatorService;
	}


	public static IScanningService getScanService() {
		return scanService;
	}


	public static void setScanService(IScanningService scanService) {
		Services.scanService = scanService;
	}


	public static IDeviceConnectorService getConnector() {
		return connector;
	}


	public static void setConnector(IDeviceConnectorService connector) {
		Services.connector = connector;
	}


	public static IMalcolmService getMalcService() {
		return malcService;
	}


	public static void setMalcService(IMalcolmService malcService) {
		Services.malcService = malcService;
	}

}
