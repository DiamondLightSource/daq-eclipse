package org.eclipse.scanning.api.malcolm.attributes;

/**
 * 
 * Encapsulates a string array attribute as read from a malcolm device
 * 
 * @author Matt Taylor
 *
 */
public class StringArrayAttribute extends MalcolmAttribute {
	public static final String STRINGARRAY_ID = "malcolm:core/StringArrayMeta:";
	
	String[] value;
	
	public StringArrayAttribute() {
		
	}
	
	public StringArrayAttribute(String[] value) {
		this.value = value;
	}

	@Override
	public String[] getValue() {
		return value;
	}

	public void setValue(String[] value) {
		this.value = value;
	}
}
