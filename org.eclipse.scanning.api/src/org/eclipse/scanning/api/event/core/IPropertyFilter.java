package org.eclipse.scanning.api.event.core;

import java.util.List;

/**
 * 
 * @author Matthew Gerring
 *
 */
public interface IPropertyFilter {

	/**
	 * It is current only possible to filter properties by deleting them.
	 *
	 */
	public enum FilterAction {
		DELETE;
	}
	
	/**
	 * 
	 * @param name
	 * @param action
	 */
	void addProperty(String name, FilterAction... action);
	
	/**
	 * Removes the filter for the named property.
	 * @param name
	 */
	void removeProperty(String name);
	
	/**
	 * 
	 * @return the list of named properties that we are filtering.
	 */
	List<String> getProperties();
}
