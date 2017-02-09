package org.eclipse.scanning.api.malcolm.attributes;

import java.util.Arrays;

/**
 * 
 * Encapsulates a boolean array attribute as read from a malcolm device
 * 
 * @author Matt Taylor
 *
 */
public class BooleanArrayAttribute extends MalcolmAttribute {
	public static final String BOOLEANARRAY_ID = "malcolm:core/BooleanArrayMeta:";
	
	private boolean value[];

	public void setValue(boolean[] value) {
		this.value = value;
	}

	@Override
	public boolean[] getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "BooleanArrayAttribute [value=" + Arrays.toString(value) + " " + super.toString() + "]";
	}
	
}
