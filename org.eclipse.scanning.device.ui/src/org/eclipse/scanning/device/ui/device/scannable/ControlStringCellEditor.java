package org.eclipse.scanning.device.ui.device.scannable;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.scanning.api.scan.ui.ControlNode;
import org.eclipse.swt.widgets.Composite;

/**
 * Cell Editor for controls that have a string value, but do not have a list
 * of permitted values.
 */
final class ControlStringCellEditor extends TextCellEditor {
	
	// TODO for Matt G.: in direct mode need to update scannable when value changed
	
	private final ControlNode controlNode;
	
	public ControlStringCellEditor(Composite parent, ControlNode controlNode) {
		super(parent);
		this.controlNode = controlNode;
	}

	protected void doSetValue(final Object value) {
		Assert.isTrue(value == controlNode);
		super.doSetValue(controlNode.getValue());
	}

	@Override
	protected Object doGetValue() {
		final String value = (String) super.doGetValue();
		controlNode.setValue(value);
		return controlNode;
	}
	
}