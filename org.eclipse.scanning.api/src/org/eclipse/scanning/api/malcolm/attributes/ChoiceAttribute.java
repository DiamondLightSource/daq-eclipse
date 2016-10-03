package org.eclipse.scanning.api.malcolm.attributes;

/**
 * 
 * Encapsulates a choice attribute as read from a malcolm device
 * 
 * @author Matt Taylor
 *
 */
public class ChoiceAttribute extends MalcolmAttribute {
	public static final String CHOICE_ID = "malcolm:core/ChoiceMeta:";
	
	private String[] choices;
	private String value;
	
	public String[] getChoices() {
		return choices;
	}

	public void setChoices(String[] choices) {
		this.choices = choices;
	}

	@Override
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}
