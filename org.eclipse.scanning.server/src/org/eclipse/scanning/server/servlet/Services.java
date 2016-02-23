package org.eclipse.scanning.server.servlet;

import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.malcolm.IMalcolmService;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.scan.IDeviceConnectorService;
import org.eclipse.scanning.api.scan.IDeviceService;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.scanning.api.scan.process.IPreprocessingService;
import org.eclipse.scanning.api.script.IScriptService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;


/**
 * This class has been created to accept the services if they
 * are there but to look for them if they are not.
 * 
 * @author Matthew Gerring
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
	
	private static BundleContext           context;

	public static IFilePathService getFilePathService() {
		if (filePathService == null) try {
		     filePathService = getService(IFilePathService.class);
		} catch (NullPointerException npe) { // No service reference found.
			filePathService = null;
		}
		return filePathService;
	}


	public static void setFilePathService(IFilePathService filePathService) {
		Services.filePathService = filePathService;
	}


	public static IEventService getEventService() {
		if (eventService == null) eventService = getService(IEventService.class);
		return eventService;
	}


	public static void setEventService(IEventService eventService) {
		Services.eventService = eventService;
	}


	public static IPointGeneratorService getGeneratorService() {
		if (generatorService == null) generatorService = getService(IPointGeneratorService.class);
		return generatorService;
	}


	public static void setGeneratorService(IPointGeneratorService generatorService) {
		Services.generatorService = generatorService;
	}


	public static IDeviceService getScanService() {
		if (scanService == null) scanService = getService(IDeviceService.class);
		return scanService;
	}


	public static void setScanService(IDeviceService scanService) {
		Services.scanService = scanService;
	}


	public static IDeviceConnectorService getConnector() {
		if (connector == null) connector = getService(IDeviceConnectorService.class);
		return connector;
	}


	public static void setConnector(IDeviceConnectorService connector) {
		Services.connector = connector;
	}


	public static IMalcolmService getMalcService() {
		if (malcService == null) malcService = getService(IMalcolmService.class);
		return malcService;
	}


	public static void setMalcService(IMalcolmService malcService) {
		Services.malcService = malcService;
	}

	public void start(BundleContext context) {
		Services.context = context;
	}
	
	private static <T> T getService(Class<T> clazz) {
		if (context == null) return null;
		ServiceReference<T> ref = context.getServiceReference(clazz);
        return context.getService(ref);
	}


	public static IPreprocessingService getPreprocessingService() {
		if (preprocessingService == null) preprocessingService = getService(IPreprocessingService.class);
		return preprocessingService;
	}


	public static void setPreprocessingService(IPreprocessingService preprocessingService) {
		Services.preprocessingService = preprocessingService;
	}


	public static IScriptService getScriptService() {
		return scriptService;
	}


	public static void setScriptService(IScriptService scriptService) {
		if (scriptService == null) scriptService = getService(IScriptService.class);
		Services.scriptService = scriptService;
	}
}
