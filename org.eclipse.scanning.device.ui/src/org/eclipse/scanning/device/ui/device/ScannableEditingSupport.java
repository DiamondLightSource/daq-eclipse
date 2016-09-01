package org.eclipse.scanning.device.ui.device;

import java.net.URI;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.scanning.api.INamedNode;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.scan.ui.ControlGroup;
import org.eclipse.scanning.api.scan.ui.ControlNode;
import org.eclipse.scanning.api.scan.ui.ControlTree;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.eclipse.scanning.device.ui.model.ModelFieldEditorFactory;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScannableEditingSupport extends EditingSupport {
	
	private static final Logger logger = LoggerFactory.getLogger(ScannableEditingSupport.class);
	private ControlView controlView;
	
	public ScannableEditingSupport(ControlView cview) {
		super(cview.getViewer());
		this.controlView = cview;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		try {
			if (element instanceof ControlNode) {
				IScannableDeviceService cservice = ServiceHolder.getEventService().createRemoteService(new URI(Activator.getJmsUri()), IScannableDeviceService.class);
				return ModelFieldEditorFactory.getScannableEditor((Composite)getViewer().getControl(), cservice);
			} 
		} catch (Exception ne) {
			logger.error("Cannot get a proper scannable editor!", ne);
		}
		return new TextCellEditor((Composite)getViewer().getControl()) {
			@Override
			protected void doSetValue(Object value) {
				if (value instanceof INamedNode) value = ((INamedNode)value).getDisplayName();
				String string = value!=null ? value.toString() : "";
				super.doSetValue(string);
			}
		};
	}

	@Override
	protected boolean canEdit(Object element) {
		INamedNode node = (INamedNode)element;
		if (controlView.isEditNode()) return true;
		return node.getName()==null || "".equals(node.getName());
	}

	@Override
	protected Object getValue(Object element) {
		INamedNode node = (INamedNode)element;
		return node instanceof ControlGroup ? node.getDisplayName() : node.getName();
	}

	@Override
	protected void setValue(Object element, Object value) {
		
		String name = (String)value;
		INamedNode node = (INamedNode)element;
		
		if (node.getName()==null || "".equals(node.getName())) {
			if (ControlTree.getInstance().contains(name)) {
				INamedNode other = ControlTree.getInstance().getNode(name);
				MessageDialog.openError(getViewer().getControl().getShell(), "Invalid Name '"+name+"'", "The name '"+name+"' is already used for another control.\n\n"
						+ "The control has a label of '"+other.getDisplayName()+"' and is linked to '"+other.getName()+"' and cannot be redefined.");
			    return;
			}
			ControlTree.getInstance().setName(node, name);
		}
		node.setDisplayName(name);
		getViewer().refresh();
	}
}
