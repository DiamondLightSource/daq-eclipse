package org.eclipse.scanning.device.ui.device.scannable;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.scan.ui.ControlNode;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.DevicePreferenceConstants;
import org.eclipse.swt.widgets.Composite;

class ControlEditingSupport extends EditingSupport {
	
	/**
	 * Cell Editor for controls that have a string value, but do not have a list
	 * of permitted values.
	 */
	private static final class ControlStringCellEditor extends TextCellEditor {
		
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
	
	private static final class ControlStringComboCellEditor extends ComboBoxCellEditor {
		
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
	
	private IScannableDeviceService cservice;
	private ControlViewerMode       mode;

	ControlEditingSupport(ColumnViewer viewer, IScannableDeviceService cservice, ControlViewerMode mode) {
		super(viewer);
		Activator.getDefault().getPreferenceStore().setDefault(DevicePreferenceConstants.NUMBER_FORMAT, "##########0.0###");
		this.cservice = cservice;
		this.mode = mode;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		Assert.isTrue(element instanceof ControlNode);
		final Composite parent = (Composite) getViewer().getControl();
		
		final ControlNode controlNode = (ControlNode) element; 
		final Object value = controlNode.getValue();
		if (value instanceof Number) {
			return new ControlValueCellEditor(parent, cservice, mode);
		} else if (value instanceof String) {
			String[] permittedValues = getPermittedValues(controlNode);
			if (permittedValues == null) {
				return new ControlStringCellEditor(parent, controlNode);
			} else {
				return new ControlStringComboCellEditor(parent, controlNode, permittedValues);
			}
		} else {
			throw new RuntimeException("Unsupported type: " + value.getClass().getName());
		}
	}
	
	private String[] getPermittedValues(ControlNode controlNode) {
		final String scannableName = controlNode.getScannableName();
		try {
			IScannable<String> scannable = cservice.getScannable(scannableName);
			return scannable.getPermittedValues();
		} catch (Exception e) {
			throw new RuntimeException("Could not get scannable " + scannableName, e);
		}
	}

	@Override
	protected boolean canEdit(Object element) {
		// can only edit ControlNodes and only when the value is not null
		// (the value could be null if the scannable doesn't exist)
		if (element instanceof ControlNode) {
			Object value = ((ControlNode) element).getValue();
			// can only edit where value is a number or string
			// (in particular cannot edit where value is null, or where it is an array of any kind)
			return value instanceof Number || value instanceof String;
		}
		return false;
	}

	@Override
	protected Object getValue(Object element) {
		return element;
	}

	@Override
	protected void setValue(Object element, Object value) {
		getViewer().refresh(element);
	}

}
