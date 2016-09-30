package org.eclipse.scanning.api.malcolm.attributes;

/**
 * 
 * Encapsulates a number array attribute as read from a malcolm device
 * 
 * @author Matt Taylor
 *
 */
public class NumberArrayAttribute extends MalcolmAttribute {
	public static final String NUMBERARRAY_ID = "malcolm:core/NumberArrayMeta:";
	
	String dtype;
	Number value[];
	
	public String getDtype() {
		return dtype;
	}

	public void setDtype(String dtype) {
		this.dtype = dtype;
	}

	@Override
	public Number[] getValue() {
		return value;
	}

	public void setValue(Number[] value) {
		this.value = value;
	}
	
}
