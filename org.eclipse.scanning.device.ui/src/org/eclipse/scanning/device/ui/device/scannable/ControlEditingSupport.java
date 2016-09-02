package org.eclipse.scanning.device.ui.device.scannable;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.scanning.api.INamedNode;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.DevicePreferenceConstants;
import org.eclipse.swt.widgets.Composite;

class ControlEditingSupport extends EditingSupport {
	
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
		// TODO Editor for non-number controls...
		return new ControlValueCellEditor((Composite)getViewer().getControl(), cservice, mode);
	}

	@Override
	protected boolean canEdit(Object element) {
		if (!(element instanceof INamedNode)) return false;
		return !((INamedNode)element).hasChildren();
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
