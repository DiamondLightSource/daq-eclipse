package org.eclipse.scanning.device.ui.device.scannable;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.scanning.api.scan.ui.ControlNode;
import org.eclipse.swt.widgets.Composite;

final class ControlStringComboCellEditor extends ComboBoxCellEditor {
	
	// TODO for Matt G.: in direct mode need to update scannable when value changed
	
	private final ControlNode controlNode;
	
	public ControlStringComboCellEditor(Composite parent, ControlNode controlNode,
			String[] permittedValues) {
		super(parent, permittedValues);
		this.controlNode = controlNode;
	}
	
	protected void doSetValue(final Object value) {
		Assert.isTrue(value == controlNode);
		// Need to set the index of the selected item in the superclass method
		final String stringValue = (String) controlNode.getValue();
		final String[] items = getItems();
		int itemIndex = 0;
		for (int i = 0; i < items.length; i++) {
			if (items[i].equals(stringValue)) {
				itemIndex = i;
				break;
			}
		}
		super.doSetValue(itemIndex);
	}
	
	protected Object doGetValue() {
		final int selectionIndex = ((Integer) super.doGetValue()).intValue();
		final String stringValue = getItems()[selectionIndex];
		controlNode.setValue(stringValue);
		return controlNode;
	}
	
}