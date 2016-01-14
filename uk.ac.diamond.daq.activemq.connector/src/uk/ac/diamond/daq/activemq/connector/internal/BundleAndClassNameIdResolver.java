package uk.ac.diamond.daq.activemq.connector.internal;

import org.osgi.framework.Bundle;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.ClassNameIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ClassUtil;

/**
 * {@link TypeIdResolver} implementation which converts between JSON strings and the information necessary to load a class in OSGi, that is,
 * the fully-qualified class name, bundle symbolic name and bundle version.
 * <p>
 * Generic types are currently not handled. It might be possible to do this (perhaps by delegating to ClassNameIdResolver) but all calls made
 * to ClassUtils would need overriding to use correct bundle classloaders.
 * <p>
 * Also, non-static inner types will probably fail with the current implementation, but this has not been tested.
 *
 * @author Colin Palmer
 *
 */
public class BundleAndClassNameIdResolver extends TypeIdResolverBase {

	/**
	 * Simple class to hold necessary information about a bundle and class, and convert it to and from string form.
	 */
	private static class BundleAndClassInfo {

		private static final String FIELD_DELIMITER = "&"; // cannot be any of ;,.-_ due to collisions with parts of allowed names
		private static final String EQUALS = "=";
		private static final String BUNDLE_FIELD_NAME = "bundle";
		private static final String VERSION_FIELD_NAME = "version";
		private static final String CLASS_FIELD_NAME = "class";

		private String bundleSymbolicName = "";
		private String bundleVersion = "";
		private String className = "";

		private BundleAndClassInfo() {}

		@Override
		public String toString() {
			return BUNDLE_FIELD_NAME + EQUALS + bundleSymbolicName + FIELD_DELIMITER
					+ VERSION_FIELD_NAME + EQUALS + bundleVersion + FIELD_DELIMITER
					+ CLASS_FIELD_NAME + EQUALS + className;
		}

		public static BundleAndClassInfo from(Bundle bundle, String className) {
			BundleAndClassInfo result = new BundleAndClassInfo();
			if (bundle != null) {
				result.bundleSymbolicName = bundle.getSymbolicName();
				result.bundleVersion = bundle.getVersion().toString();
			}
			result.className = className;
			return result;
		}

		public static BundleAndClassInfo from(String id) {
			BundleAndClassInfo result = new BundleAndClassInfo();
			for (String idPart : id.split(FIELD_DELIMITER)) {
				if (idPart.startsWith(BUNDLE_FIELD_NAME)) {
					result.bundleSymbolicName = idPart.substring(idPart.indexOf(EQUALS) + 1);
				} else if (idPart.startsWith(VERSION_FIELD_NAME)) {
					result.bundleVersion = idPart.substring(idPart.indexOf(EQUALS) + 1);
				} else if (idPart.startsWith(CLASS_FIELD_NAME)) {
					result.className = idPart.substring(idPart.indexOf(EQUALS) + 1);
				}
			}
			return result;
		}
	}

	private final BundleProvider bundleProvider;
	private final ClassNameIdResolver classNameIdResolver;

	public BundleAndClassNameIdResolver(JavaType baseType, TypeFactory typeFactory, BundleProvider bundleProvider) {
		super(baseType, typeFactory);
		this.bundleProvider = bundleProvider;

		// Create a ClassNameIdResolver to delegate to when handling class names (i.e. after we have handled the bundle information)
		this.classNameIdResolver = new ClassNameIdResolver(baseType, typeFactory);
	}

	@Override
	public Id getMechanism() { return Id.CUSTOM; }

	@Override
	public String idFromValue(Object value) {
		return idFromValueAndType(value, value.getClass());
	}

	@Override
	public String idFromValueAndType(Object value, Class<?> clazz) {
		Bundle bundle = bundleProvider.getBundle(clazz);
		String className = classNameIdResolver.idFromValueAndType(value, clazz);
		BundleAndClassInfo info = BundleAndClassInfo.from(bundle, className);
		return info.toString();
	}

	@Override
	public JavaType typeFromId(String id) {
		BundleAndClassInfo info = BundleAndClassInfo.from(id);
		Class<?> clazz;
		try {
			clazz = getClass(info);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Class " + id + " not found", e);
		}
		// TODO this probably doesn't handle generics, except for arrays and collections
		// see ClassNameIdResolver#typeFromId() for more on this
		JavaType type = _typeFactory.constructSpecializedType(_baseType, clazz);
		return type;
	}

	private Class<?> getClass(BundleAndClassInfo info) throws ClassNotFoundException {

		// If there is no bundle name, try loading the class using the standard Jackson utility method
		// TODO see ClassNameIdResolver for complexity regarding generics here - not supported for now
		if (info.bundleSymbolicName.length() == 0) {
			return ClassUtil.findClass(info.className);
		}

		// TODO FIXME cache bundles for performance when looking up the same class repeatedly
		// not sure whether to cache bundle itself or bundle id
		Bundle[] bundles = bundleProvider.getBundles();
		Bundle bundleToUse = null;
		for (Bundle bundle : bundles) {
			if (info.bundleSymbolicName.equals(bundle.getSymbolicName())
					&& bundle.getVersion().toString().equals(info.bundleVersion)) {
				bundleToUse = bundle;
				break;
				// TODO cache bundles with incorrect version and try them if correct version is not found?
			}
		}
		if (bundleToUse != null) {
			try {
				return bundleToUse.loadClass(info.className);
			} catch (ClassNotFoundException | IllegalStateException ignored) {
				// the bundle cannot find the required class, so we ignore the exception and fall back to just finding the class by name
			}
		}
		// If the bundle is not found, or cannot load the required class, fall back and try to load the class with ClassUtil
		return ClassUtil.findClass(info.className);
	}
}
