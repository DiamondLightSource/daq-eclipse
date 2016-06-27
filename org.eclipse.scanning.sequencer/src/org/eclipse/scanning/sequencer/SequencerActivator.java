package org.eclipse.scanning.sequencer;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class SequencerActivator implements BundleActivator {

	private static BundleContext context;

	@Override
	public void start(BundleContext c) throws Exception {
		context = c;
	}

	@Override
	public void stop(BundleContext c) throws Exception {
		context = null;
	}

	public static <T> T getService(Class<T> serviceClass) {
		ServiceReference<T> ref = context.getServiceReference(serviceClass);
		return context.getService(ref);
	}
	
	public static Object getService(String serviceClass) {
		ServiceReference<?> ref = context.getServiceReference(serviceClass);
		return context.getService(ref);
	}

	public static boolean isStarted() {
		return context!=null;
	}

}
