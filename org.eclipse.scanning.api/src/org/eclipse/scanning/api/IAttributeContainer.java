package org.eclipse.scanning.api;

import java.util.Collection;

/**
 * Interface to allow the getting and setting of attributes.
 * 
 * @author Matthew Gerring
 *
 */
public interface IAttributeContainer {
	
	/**
	 * 
	 * @return null if no attributes, otherwise collection of the names of the attributes set
	 */
	default Collection<String> getAttributeNames() {
		return null;
	}

	/**
	 * Set any attribute the implementing classes may provide
	 * 
	 * @param attributeName
	 *            is the name of the attribute
	 * @param value
	 *            is the value of the attribute
	 * @throws DeviceException
	 *             if an attribute cannot be set
	 */
	default <T> void setAttribute(String attributeName, T value) throws Exception {
		// Do nothing
	}

	/**
	 * Get the value of the specified attribute
	 * 
	 * @param attributeName
	 *            is the name of the attribute
	 * @return the value of the attribute
	 * @throws DeviceException
	 *             if an attribute cannot be retrieved
	 */
	default <T> T getAttribute(String attributeName) throws Exception {
		return null;
	}

}
