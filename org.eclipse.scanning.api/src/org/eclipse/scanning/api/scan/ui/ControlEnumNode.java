package org.eclipse.scanning.api.scan.ui;

import org.eclipse.scanning.api.INameable;

public class ControlEnumNode extends AbstractControl implements INameable {

	private Enum value;
	
	public ControlEnumNode() {
	}
	
	public ControlEnumNode(String parentName, String name, Enum value) {
		setName(name);
		setParentName(parentName);
		this.value = value;
	}

	public String[] getChoices() {
		Enum[] values = value.getClass().getEnumConstants();
		String[] ret = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			ret[i] = values[i].name();
		}
		return ret;
	}

	public Enum getValue() {
		return value;
	}

	public void setValue(Enum value) {
		this.value = value;
	}
	
}
