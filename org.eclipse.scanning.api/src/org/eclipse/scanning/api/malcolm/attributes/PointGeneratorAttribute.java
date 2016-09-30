package org.eclipse.scanning.api.malcolm.attributes;

/**
 * 
 * Encapsulates a PointGenerator attribute as read from a malcolm device
 * 
 * @author Matt Taylor
 *
 */
public class PointGeneratorAttribute extends MalcolmAttribute {
	public static final String POINTGENERATOR_ID = "malcolm:core/PointGenerator:";
	
	Object value;

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public Object getValueAsObject() {
		return value;
	}
	
}
