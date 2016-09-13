package org.eclipse.scanning.device.ui.device.scannable;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.scan.ui.ControlNode;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.DevicePreferenceConstants;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ControlEditingSupport extends EditingSupport {
	
	private static final Logger logger = LoggerFactory.getLogger(ControlEditingSupport.class);
	
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
		Object value = controlNode.getValue();
		if (value==null && mode.isDirectlyConnected()) {
			try {
				value = cservice.getScannable(controlNode.getName()).getPosition();
			} catch (Exception e) {
				logger.error("Unable to connect to server!", e);
				value = "Error connecting to server";
			}
		}
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
		
		if (mode.isDirectlyConnected()) return true;
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
