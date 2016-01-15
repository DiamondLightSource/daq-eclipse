package uk.ac.diamond.daq.activemq.connector.test;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle;

import uk.ac.diamond.daq.activemq.connector.internal.BundleProvider;

/**
 * A BundleProvider implementation for use in unit tests.
 * <p>
 * Class objects can be registered along with a Bundle (which could well be a mock). When asked for the bundle which
 * loaded a class, the registered bundle is returned. The list of all registered bundles can also be returned.
 *
 * @author Colin Palmer
 *
 */
public class TestBundleProvider implements BundleProvider {

	private static final Bundle[] EMPTY_BUNDLE_ARRAY = new Bundle[0];

	private Map<Class<?>, Bundle> classBundleMap = new HashMap<>();

	public void registerBundleForClass(Class<?> clazz, Bundle bundle) {
		classBundleMap.put(clazz, bundle);
	}

	@Override
	public Bundle getBundle(Class<?> clazz) {
		return classBundleMap.get(clazz);
	}

	@Override
	public Bundle[] getBundles() {
		return classBundleMap.values().toArray(EMPTY_BUNDLE_ARRAY);
	}
}
