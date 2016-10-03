package org.eclipse.scanning.api.annotation.ui;

public enum FieldRole {

	SIMPLE, EXPERT, ALL;

	public String getLabel() {
		return toString().substring(0, 1)+toString().toLowerCase().substring(1);
	}
}
