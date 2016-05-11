package org.eclipse.scanning.sequencer;

import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.nexus.builder.NexusBuilderFactory;

public class ServiceHolder {
	
	// OSGi stuff
	private static NexusBuilderFactory factory;

	public static NexusBuilderFactory getFactory() {
		return factory;
	}

	public void setFactory(NexusBuilderFactory factory) {
		ServiceHolder.factory = factory;
	}

    private static IOperationService operationService;

	public static IOperationService getOperationService() {
		return operationService;
	}

	public void setOperationService(IOperationService operationService) {
		ServiceHolder.operationService = operationService;
	}
    
	private static IPersistenceService persistenceService;

	public static IPersistenceService getPersistenceService() {
		return persistenceService;
	}

	public void setPersistenceService(IPersistenceService persistenceService) {
		ServiceHolder.persistenceService = persistenceService;
	}
	
	private static ILoaderService loaderService;

	public static ILoaderService getLoaderService() {
		return loaderService;
	}

	public void setLoaderService(ILoaderService loaderService) {
		ServiceHolder.loaderService = loaderService;
	}

	/**
	 * Used to provide servcices when tests running in non-OSGi mode.
	 * @param ls
	 * @param defaultNexusBuilderFactory
	 */
	public static void setTestServices(ILoaderService ls, NexusBuilderFactory defaultNexusBuilderFactory) {
		loaderService = ls;
		factory = defaultNexusBuilderFactory;
	}
	
}
