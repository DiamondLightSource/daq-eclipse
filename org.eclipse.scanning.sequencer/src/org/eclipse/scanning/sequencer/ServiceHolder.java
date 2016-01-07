package org.eclipse.scanning.sequencer;

import org.eclipse.dawnsci.nexus.builder.NexusBuilderFactory;

public class ServiceHolder {
	
	// OSGi stuff
	private static NexusBuilderFactory factory;

	public static NexusBuilderFactory getFactory() {
		return factory;
	}

	public static void setFactory(NexusBuilderFactory factory) {
		ServiceHolder.factory = factory;
	}

}
