package org.eclipse.scanning.api;

/**
 * 
 * This interface exists to provide a mechanism for scanning.api to
 * find services from OSGi without making a dependency on OSGi directy
 * or having an activator of this bundle. It means that the API bundle
 * stays totally vanilla which is a goal such that anyone may use
 * service interfaces it defines regardless of mechanism.
 * 
 * @author Matthew Gerring
 *
 */
public interface IServiceResolver {

	/**
	 * Method comparible to getting a service from OSGi without making a dependency
	 * on OGSi in this bundle.
	 * 
	 * @param serviceClass
	 * @return
	 */
	<T> T getService(Class<T> serviceClass);
}
