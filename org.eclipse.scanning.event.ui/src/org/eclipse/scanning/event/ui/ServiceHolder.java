package org.eclipse.scanning.event.ui;

import org.eclipse.dawnsci.analysis.api.expressions.IExpressionService;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.points.IPointGeneratorService;

public class ServiceHolder {

	private static IEventConnectorService eventConnectorService;
	private static IEventService          eventService;
	private static IPointGeneratorService generatorService;
	private static IExpressionService     expressionService;
	private static ILoaderService         loaderService;

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

	public static IExpressionService getExpressionService() {
		return expressionService;
	}

	public static void setExpressionService(IExpressionService expressionService) {
		ServiceHolder.expressionService = expressionService;
	}

	public static ILoaderService getLoaderService() {
		return loaderService;
	}

	public static void setLoaderService(ILoaderService loaderService) {
		ServiceHolder.loaderService = loaderService;
	}
}
