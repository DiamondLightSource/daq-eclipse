package org.eclipse.scanning.device.ui.device;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.scanning.api.INamedNode;
import org.eclipse.swt.widgets.Composite;

class ControlEditingSupport extends EditingSupport {
	
	ControlEditingSupport(ColumnViewer viewer) {
		super(viewer);
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return new ControlCellEditor((Composite)getViewer().getControl());
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
		// Not required, the scannable value is set directly by the control.
	}

}
