package org.eclipse.scanning.server.servlet;

import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.malcolm.IMalcolmService;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.scan.IDeviceConnectorService;
import org.eclipse.scanning.api.scan.IDeviceService;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.scanning.api.scan.process.IPreprocessingService;
import org.eclipse.scanning.api.script.IScriptService;


/**
 * This class holds services for the scanning server servlets. Services should be configured to be optional and dynamic
 * and will then be injected correctly by Equinox DS.
 *
 * @author Matthew Gerring
 * @author Colin Palmer
 *
 */
public class Services {

	private static IEventService           eventService;
	private static IPointGeneratorService  generatorService;
	private static IDeviceService          scanService;
	private static IDeviceConnectorService connector;
	private static IMalcolmService         malcService;
	private static IFilePathService        filePathService;
	private static IPreprocessingService   preprocessingService;
	private static IScriptService          scriptService;

	public static IFilePathService getFilePathService() {
		return filePathService;
	}

	public static void setFilePathService(IFilePathService filePathService) {
		Services.filePathService = filePathService;
	}

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

	public static IDeviceService getScanService() {
		return scanService;
	}

	public static void setScanService(IDeviceService scanService) {
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

	public static IPreprocessingService getPreprocessingService() {
		return preprocessingService;
	}

	public static void setPreprocessingService(IPreprocessingService preprocessingService) {
		Services.preprocessingService = preprocessingService;
	}

	public static IScriptService getScriptService() {
		return scriptService;
	}

	public static void setScriptService(IScriptService scriptService) {
		Services.scriptService = scriptService;
	}
}
