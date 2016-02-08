package org.eclipse.scanning.server.servlet;

import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.malcolm.IMalcolmService;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.scan.IDeviceConnectorService;
import org.eclipse.scanning.api.scan.IDeviceService;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Services {

	private static IEventService           eventService;
	private static IPointGeneratorService  generatorService;
	private static IDeviceService          scanService;
	private static IDeviceConnectorService connector;
	private static IMalcolmService         malcService;
	private static IFilePathService        filePathService;
	private static BundleContext           context;

	public static IFilePathService getFilePathService() {
		if (filePathService == null && context!=null) {
			ServiceReference<IFilePathService> ref = context.getServiceReference(IFilePathService.class);
			if (ref!=null) filePathService = context.getService(ref);
		}
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
		if (connector == null && context!=null) {
			ServiceReference<IDeviceConnectorService> ref = context.getServiceReference(IDeviceConnectorService.class);
			if (ref!=null) connector = context.getService(ref);
		}
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

	public void start(BundleContext context) {
		Services.context = context;
	}
}
