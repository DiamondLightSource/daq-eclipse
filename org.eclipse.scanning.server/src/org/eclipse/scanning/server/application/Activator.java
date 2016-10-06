package org.eclipse.scanning.server.application;

import org.eclipse.scanning.api.IServiceResolver;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

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

	public static BundleContext getContext() {
		return context;
	}
	
	public static IServiceResolver createResolver() {
		if (context == null) return null;
		return new IServiceResolver() {
			@Override
			public <T> T getService(Class<T> serviceClass) {
				ServiceReference<T> ref = context.getServiceReference(serviceClass);
				return context.getService(ref);
			}
		};
	}
	

}
