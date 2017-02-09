package org.eclipse.scanning.api.malcolm.attributes;

import java.util.Arrays;

/**
 * 
 * Encapsulates a number array attribute as read from a malcolm device
 * 
 * @author Matt Taylor
 *
 */
public class NumberArrayAttribute extends MalcolmAttribute {
	public static final String NUMBERARRAY_ID = "malcolm:core/NumberArrayMeta:";
	
	private String dtype;
	private Number value[];
	
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

	@Override
	public String toString() {
		return "NumberArrayAttribute [dtype=" + dtype + ", value=" + Arrays.toString(value) + ", " + super.toString() + "]";
	}

}
