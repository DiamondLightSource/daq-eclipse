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

	ControlEditingSupport(ColumnViewer viewer, IScannableDeviceService cservice) {
		super(viewer);
		Activator.getDefault().getPreferenceStore().setDefault(DevicePreferenceConstants.NUMBER_FORMAT, "##########0.0###");
		this.cservice = cservice;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return new ControlCellEditor((Composite)getViewer().getControl(), cservice);
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
