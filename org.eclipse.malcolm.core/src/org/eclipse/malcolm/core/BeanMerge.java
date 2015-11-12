package org.eclipse.malcolm.core;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

public class BeanMerge {

	/**
	 * Puts the template values in the bean, makes it easy
	 * to send events without having to set all properties
	 * in each event manually.
	 * @param event
	 * @throws IntrospectionException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	public static <M> void merge(M template, M destination) throws IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		BeanInfo beanInfo = Introspector.getBeanInfo(template.getClass());

		// Iterate over all the attributes
		for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {

			// Only copy writable attributes
			if (descriptor.getWriteMethod() != null) {
				Object originalValue = descriptor.getReadMethod().invoke(destination);

				// Only copy values values where the destination values is null
				if (originalValue == null) {
					Object defaultValue = descriptor.getReadMethod().invoke(template);
					descriptor.getWriteMethod().invoke(destination, defaultValue);
				}

			}
		}
	}


}
