package org.eclipse.scanning.device.ui;

import org.eclipse.dawnsci.analysis.api.expressions.IExpressionService;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.io.IRemoteDatasetService;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.scanning.api.ISpringParser;
import org.eclipse.scanning.api.IValidatorService;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;

public class ServiceHolder {

	private static IEventConnectorService eventConnectorService;
	private static IEventService          eventService;
	private static IPointGeneratorService generatorService;
	private static IValidatorService      validatorService;
	private static IExpressionService     expressionService;
	private static ILoaderService         loaderService;
	private static ISpringParser          springParser;
	private static IMarshallerService     marshallerService;
	private static IPlottingService       plottingService;
	private static IRemoteDatasetService  remoteDatasetService;
	private static EventAdmin             eventAdmin;
	
	private static BundleContext context;

	public static IEventConnectorService getEventConnectorService() {
		if (eventConnectorService==null) eventConnectorService = getService(IEventConnectorService.class);
		return eventConnectorService;
	}

	public void setEventConnectorService(IEventConnectorService eventService) {
		ServiceHolder.eventConnectorService = eventService;
	}

	public static IEventService getEventService() {
		if (eventService==null) eventService = getService(IEventService.class);
		return eventService;
	}

	public void setEventService(IEventService eventService) {
		ServiceHolder.eventService = eventService;
	}

	public static IPointGeneratorService getGeneratorService() {
		if (generatorService==null) generatorService = getService(IPointGeneratorService.class);
		return generatorService;
	}

	public void setGeneratorService(IPointGeneratorService generatorService) {
		ServiceHolder.generatorService = generatorService;
	}

	public static IExpressionService getExpressionService() {
		if (expressionService==null) expressionService = getService(IExpressionService.class);
		
		// Use the EmergencyExpressionService, might fail is jexl not there (this is allowed)
		if (expressionService==null) try {
			 // Does more limited things but works.
			expressionService = new org.eclipse.scanning.device.ui.util.EmergencyExpressionService();
		} catch (Exception ignored) {
			// It is allowed for no JEXL to be in CP.
		}
		return expressionService;
	}

	public void setExpressionService(IExpressionService expressionService) {
		ServiceHolder.expressionService = expressionService;
	}

	public static ILoaderService getLoaderService() {
		if (loaderService==null) loaderService = getService(ILoaderService.class);
		return loaderService;
	}

	public void setLoaderService(ILoaderService loaderService) {
		ServiceHolder.loaderService = loaderService;
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

	public static IValidatorService getValidatorService() {
		if (validatorService==null) validatorService = getService(IValidatorService.class);
		return validatorService;
	}

	public static void setValidatorService(IValidatorService validatorService) {
		ServiceHolder.validatorService = validatorService;
	}

	public static ISpringParser getSpringParser() {
		if (springParser==null) springParser = getService(ISpringParser.class);
		return springParser;
	}

	public void setSpringParser(ISpringParser springParser) {
		ServiceHolder.springParser = springParser;
	}

	public static IMarshallerService getMarshallerService() {
		if (marshallerService==null) marshallerService = getService(IMarshallerService.class);
		return marshallerService;
	}

	public static void setMarshallerService(IMarshallerService marshallerService) {
		ServiceHolder.marshallerService = marshallerService;
	}

	public static IPlottingService getPlottingService() {
		if (plottingService==null) plottingService = getService(IPlottingService.class);
		return plottingService;
	}

	public static void setPlottingService(IPlottingService plottingService) {
		ServiceHolder.plottingService = plottingService;
	}

	public static IRemoteDatasetService getRemoteDatasetService() {
		if (remoteDatasetService==null) remoteDatasetService = getService(IRemoteDatasetService.class);
		return remoteDatasetService;
	}

	public static void setRemoteDatasetService(IRemoteDatasetService remoteDatasetService) {
		ServiceHolder.remoteDatasetService = remoteDatasetService;
	}

	public static EventAdmin getEventAdmin() {
		if (eventAdmin==null) eventAdmin = getService(EventAdmin.class);
		return eventAdmin;
	}

	public static void setEventAdmin(EventAdmin eventAdmin) {
		ServiceHolder.eventAdmin = eventAdmin;
	}
}
