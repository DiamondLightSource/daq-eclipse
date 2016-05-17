package org.eclipse.scanning.event.ui;

import java.net.URI;

import org.eclipse.dawnsci.analysis.api.expressions.IExpressionService;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.scanning.api.device.IDeviceConnectorService;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.event.ui.preference.CommandConstants;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class ServiceHolder {

	private static IEventConnectorService eventConnectorService;
	private static IEventService          eventService;
	private static IPointGeneratorService generatorService;
	private static IExpressionService     expressionService;
	private static ILoaderService         loaderService;
	private static IDeviceConnectorService deviceConnectorService;
	private static BundleContext context;

	public static IEventConnectorService getEventConnectorService() {
		if (eventConnectorService==null) eventConnectorService = getService(IEventConnectorService.class);
		return eventConnectorService;
	}

	public static void setEventConnectorService(IEventConnectorService eventService) {
		ServiceHolder.eventConnectorService = eventService;
	}

	public static IEventService getEventService() {
		if (eventService==null) eventService = getService(IEventService.class);
		return eventService;
	}

	public static void setEventService(IEventService eventService) {
		ServiceHolder.eventService = eventService;
	}

	public static IPointGeneratorService getGeneratorService() {
		if (generatorService==null) generatorService = getService(IPointGeneratorService.class);
		return generatorService;
	}

	public static void setGeneratorService(IPointGeneratorService generatorService) {
		ServiceHolder.generatorService = generatorService;
	}

	public static IExpressionService getExpressionService() {
		if (expressionService==null) expressionService = getService(IExpressionService.class);
		
		// Use the EmergencyExpressionService, might fail is jexl not there (this is allowed)
		if (expressionService==null) try {
			 // Does more limited things but works.
			expressionService = new org.eclipse.scanning.scanning.ui.util.EmergencyExpressionService();
		} catch (Exception ignored) {
			// It is allowed for no JEXL to be in CP.
		}
		return expressionService;
	}

	public static void setExpressionService(IExpressionService expressionService) {
		ServiceHolder.expressionService = expressionService;
	}

	public static ILoaderService getLoaderService() {
		if (loaderService==null) loaderService = getService(ILoaderService.class);
		return loaderService;
	}

	public static void setLoaderService(ILoaderService loaderService) {
		ServiceHolder.loaderService = loaderService;
	}

	public static IDeviceConnectorService getDeviceConnectorService() {
		if (deviceConnectorService==null) deviceConnectorService = getService(IDeviceConnectorService.class);
		return deviceConnectorService;
	}

	public static void setDeviceConnectorService(IDeviceConnectorService deviceConnectorService) {
		ServiceHolder.deviceConnectorService = deviceConnectorService;
	}

	public void start(BundleContext context) {
		ServiceHolder.context = context;
	}
	
	private static <T> T getService(Class<T> clazz) {
		if (context == null) return null;
		try {
			ServiceReference<T> ref = context.getServiceReference(clazz);
	        return context.getService(ref);
		} catch (NullPointerException npe) {
			return null;
		}
	}
}
