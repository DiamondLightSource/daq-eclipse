package org.eclipse.scanning.device.ui.points;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.swt.widgets.Composite;

public class AxesEditingSupport extends EditingSupport {

	private IScannableDeviceService cservice;

	public AxesEditingSupport(ColumnViewer viewer, IScannableDeviceService cservice) {
		super(viewer);
		this.cservice = cservice;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return new AxesCellEditor((Composite)getViewer().getControl(), cservice);
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
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
