package org.eclipse.scanning.sequencer;

import org.eclipse.scanning.api.IServiceResolver;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class SequencerActivator implements BundleActivator, IServiceResolver {

	private static BundleContext      context;
	private static SequencerActivator instance;

	@Override
	public void start(BundleContext c) throws Exception {
		context = c;
		instance = this;
	}

	@Override
	public void stop(BundleContext c) throws Exception {
		context = null;
		instance = null;
	}

	@Override
	public <T> T getService(Class<T> serviceClass) {
		ServiceReference<T> ref = context.getServiceReference(serviceClass);
		return context.getService(ref);
	}
	
	public Object getService(String serviceClass) {
		ServiceReference<?> ref = context.getServiceReference(serviceClass);
		return context.getService(ref);
	}

	public static boolean isStarted() {
		return context!=null;
	}

	public static IServiceResolver getInstance() {
		return instance;
	}

}
