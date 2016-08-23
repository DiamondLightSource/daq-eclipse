package org.eclipse.scanning.device.ui.device;

import java.net.URI;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.scanning.api.INamedNode;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.scan.ui.ControlNode;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.eclipse.scanning.device.ui.model.ModelFieldEditorFactory;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScannableEditingSupport extends EditingSupport {
	
	private static final Logger logger = LoggerFactory.getLogger(ScannableEditingSupport.class);

	public ScannableEditingSupport(TreeViewer viewer) {
		super(viewer);
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		try {
			IScannableDeviceService cservice = ServiceHolder.getEventService().createRemoteService(new URI(Activator.getJmsUri()), IScannableDeviceService.class);
			return ModelFieldEditorFactory.getScannableEditor((Composite)getViewer().getControl(), cservice);
		} catch (Exception ne) {
			logger.error("Cannot get a proper scannable editor!", ne);
			return new TextCellEditor((Composite)getViewer().getControl());
		}
	}

	@Override
	protected boolean canEdit(Object element) {
		INamedNode node = (INamedNode)element;
		return node.getName()==null || "".equals(node.getName());
	}

	@Override
	protected Object getValue(Object element) {
		INamedNode node = (INamedNode)element;
		return node.getName();
	}

	@Override
	protected void setValue(Object element, Object value) {
		INamedNode node = (INamedNode)element;
		node.setName((String)value);
		getViewer().refresh();
	}

}
