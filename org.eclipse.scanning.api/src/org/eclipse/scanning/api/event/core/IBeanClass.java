package org.eclipse.scanning.api.event.core;

public interface IBeanClass<T> {

	/**
	 * Class of bean usually extending StatusBean
	 * 
	 * @return class or null
	 */
	public Class<T> getBeanClass();

	/**
	 * Class of bean usually extending StatusBean
	 * 
	 * It is not compulsory to set the bean class unless trying to deserialize messages sent by older versions of the connector service.
	 */
	public void setBeanClass(Class<T> beanClass);

}
