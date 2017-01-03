package org.eclipse.scanning.api.annotation.ui;

public enum EditType {

	/**
	 * Directly edit the value using a cell editor appropriate for the value.
	 */
	DIRECT,
	
	/**
	 * Edit using a dialog because value is too long to be edited in place.
	 */
	LONG,

	/**
	 * Popup a form with a table for editing the values because there is more than one.
	 */
	COMPOUND;
}
