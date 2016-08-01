package org.eclipse.scanning.server.application;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	
	private static BundleContext context;

	@Override
	public void start(BundleContext c) throws Exception {
		context = c;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		context = null;
	}
	
	static <S> void registerService(Class<S> interfaceClass, S serviceInstance) {
		context.registerService(interfaceClass, serviceInstance, null);
	}

}
