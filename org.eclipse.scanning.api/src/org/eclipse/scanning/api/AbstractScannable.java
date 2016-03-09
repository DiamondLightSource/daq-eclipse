package org.eclipse.scanning.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * Convenience class using inheritance to contain some of the general 
 * things a scannable does that are the same for all scannables.
 * 
 * NOTE: Inheritance is designed to have three levels only
 * IScannable->AbstractScannable->A device
 * 
 * The preferred alternative if more complex behaviour is required would
 * be to create delegates for these interfaces which are then aggregated
 * in the device.
 * 
 * @author Matthew Gerring
 *
 * @param <T>
 */
public abstract class AbstractScannable<T> implements IScannable<T>, IAttributeContainer {

	private Map<String, Object> attributes;
	private int                 level;
	private String              name;
	
	protected AbstractScannable() {
		attributes = new HashMap<>(7); // TODO 
	}
	
	
	/**
	 * 
	 * @return null if no attributes, otherwise collection of the names of the attributes set
	 */
	public Collection<String> getAttributeNames() {
		return attributes.keySet();
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
	public <A> void setAttribute(String attributeName, A value) throws Exception {
		attributes.put(attributeName, (A)value);
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
	@SuppressWarnings("unchecked")
	public <A> A getAttribute(String attributeName) throws Exception {
		return (A)attributes.get(attributeName);
	}
	
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}
