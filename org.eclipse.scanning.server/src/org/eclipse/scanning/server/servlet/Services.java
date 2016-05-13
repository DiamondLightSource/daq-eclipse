package org.eclipse.scanning.server.servlet;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.scanning.api.device.IDeviceConnectorService;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.malcolm.IMalcolmService;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.scanning.api.scan.process.IPreprocessor;
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
	private static IRunnableDeviceService  runnableDeviceService;
	private static IDeviceConnectorService connector;
	private static IMalcolmService         malcService;
	private static IFilePathService        filePathService;
	private static IScriptService          scriptService;

	private static final Set<IPreprocessor> preprocessors = new LinkedHashSet<>();

	public static IFilePathService getFilePathService() {
		return filePathService;
	}

	public void setFilePathService(IFilePathService filePathService) {
		Services.filePathService = filePathService;
	}

	public static IEventService getEventService() {
		return eventService;
	}

	public void setEventService(IEventService eventService) {
		Services.eventService = eventService;
	}

	public static IPointGeneratorService getGeneratorService() {
		return generatorService;
	}

	public void setGeneratorService(IPointGeneratorService generatorService) {
		Services.generatorService = generatorService;
	}

	public static IRunnableDeviceService getRunnableDeviceService() {
		return runnableDeviceService;
	}

	public void setRunnableDeviceService(IRunnableDeviceService deviceService) {
		Services.runnableDeviceService = deviceService;
	}

	public static IDeviceConnectorService getConnector() {
		return connector;
	}

	public void setConnector(IDeviceConnectorService connector) {
		Services.connector = connector;
	}

	public static IMalcolmService getMalcService() {
		return malcService;
	}

	public void setMalcService(IMalcolmService malcService) {
		Services.malcService = malcService;
	}

	public static IScriptService getScriptService() {
		return scriptService;
	}

	public void setScriptService(IScriptService scriptService) {
		Services.scriptService = scriptService;
	}

	public static synchronized void addPreprocessor(IPreprocessor preprocessor) {
		preprocessors.add(preprocessor);
	}

	public static synchronized void removePreprocessor(IPreprocessor preprocessor) {
		preprocessors.remove(preprocessor);
	}

	public static Collection<IPreprocessor> getPreprocessors() {
		return preprocessors;
	}
}
