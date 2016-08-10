package org.eclipse.scanning.device.ui.model;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.scanning.api.annotation.ui.FieldValue;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Opens a Model Table on an additional popup for those
 * fields which are an almagom of values. For instance BoundingBox.
 * 
 * @author Matthew Gerring
 *
 */
public class ModelCellEditor extends DialogCellEditor {
	
	private static final Logger logger = LoggerFactory.getLogger(ModelCellEditor.class);
	
	private FieldValue value;

	private ILabelProvider labelProv;

	public ModelCellEditor(Composite      parent, 
			               FieldValue     value, 
			               ILabelProvider labelProv) {
		super(parent);
		this.value     = value;
		this.labelProv = labelProv;
	}

	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
		
		final ModelDialog dialog = new ModelDialog(cellEditorWindow.getShell()); // extends BeanDialog
		dialog.create();
		dialog.getShell().setSize(550,450); // As needed
		dialog.getShell().setText("Edit "+value.getAnnotation().label());
		
		try {
			dialog.setModel(value.get(true));
	        final int ok = dialog.open();
	        if (ok == Dialog.OK) {
	            return dialog.getModel();
	        }
		} catch (Exception ne) {
			logger.error("Problem editing model!", ne);
		}
		return null;
	}
	
	protected void updateContents(Object value) {
		if ( getDefaultLabel() == null) {
			return;
		}
		if (value == null ) return;
		getDefaultLabel().setText(labelProv.getText(value));
	}

}
