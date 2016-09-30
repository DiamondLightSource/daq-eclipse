package org.eclipse.scanning.api.malcolm.attributes;

/**
 * 
 * Encapsulates a string attribute as read from a malcolm device
 * 
 * @author Matt Taylor
 *
 */
public class StringAttribute extends MalcolmAttribute {
	
	public static final String STRING_ID = "malcolm:core/StringMeta:";
	
	String value;

	@Override
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
