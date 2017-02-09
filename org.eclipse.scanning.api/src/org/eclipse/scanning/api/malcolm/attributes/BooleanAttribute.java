package org.eclipse.scanning.api.malcolm.attributes;

/**
 * 
 * Encapsulates a boolean attribute as read from a malcolm device
 * 
 * @author Matt Taylor
 *
 */
public class BooleanAttribute extends MalcolmAttribute {
	public static final String BOOLEAN_ID = "malcolm:core/BooleanMeta:";
	
	private boolean value;

	@Override
	public Boolean getValue() {
		return value;
	}

	public void setValue(boolean value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "BooleanAttribute [value=" + value + ", " + super.toString() + "]";
	}
	
}
