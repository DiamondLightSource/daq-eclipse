package org.eclipse.scanning.api.malcolm.attributes;

/**
 * 
 * Encapsulates a number attribute as read from a malcolm device
 * 
 * @author Matt Taylor
 *
 */
public class NumberAttribute extends MalcolmAttribute {
	
	public transient static final String NUMBER_ID = "malcolm:core/NumberMeta:";
	
	String dtype;
	Number value;
	
	public String getDtype() {
		return dtype;
	}

	public void setDtype(String dtype) {
		this.dtype = dtype;
	}

	@Override
	public Number getValue() {
		return value;
	}

	public void setValue(Number value) {
		this.value = value;
	}
	
}
