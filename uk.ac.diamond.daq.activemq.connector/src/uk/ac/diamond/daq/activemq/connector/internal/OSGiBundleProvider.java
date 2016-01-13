package uk.ac.diamond.daq.activemq.connector.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import uk.ac.diamond.daq.activemq.connector.Activator;

/**
 * The default implementation of BundleProvider for use in an OSGi environment.
 * <p>
 * It should also be safe to use this implementation without OSGi running. The methods will return <code>null</code> or
 * empty objects but should not throw exceptions.
 *
 * @author Colin Palmer
 *
 */
public class OSGiBundleProvider implements BundleProvider {

	private static final Bundle[] NO_BUNDLES = new Bundle[0];

	@Override
	public Bundle getBundle(Class<?> clazz) {
		return FrameworkUtil.getBundle(clazz);
	}

	@Override
	public Bundle[] getBundles() {
		if (Activator.getContext() != null) {
			return Activator.getContext().getBundles();
		}
		return NO_BUNDLES;
	}
}
