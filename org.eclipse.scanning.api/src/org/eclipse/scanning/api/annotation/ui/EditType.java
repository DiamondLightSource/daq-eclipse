package org.eclipse.scanning.api.annotation.ui;

public enum EditType {

	/**
	 * Directly edit the value using a cell editor appropriate for the value.
	 */
	DIRECT,
	
	/**
	 * Popup a form with a table for editing the values because there is more than one.
	 */
	COMPOUND;
}
