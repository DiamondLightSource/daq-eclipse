package org.eclipse.scanning.api.malcolm.attributes;

/**
 * 
 * Encapsulates a boolean array attribute as read from a malcolm device
 * 
 * @author Matt Taylor
 *
 */
public class BooleanArrayAttribute extends MalcolmAttribute {
	public static final String BOOLEANARRAY_ID = "malcolm:core/BooleanArrayMeta:";
	
	boolean value[];

	public boolean[] getValue() {
		return value;
	}

	public void setValue(boolean[] value) {
		this.value = value;
	}

	@Override
	public Object getValueAsObject() {
		return value;
	}
	
}
