package org.eclipse.scanning.device.ui.model;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scanning.api.annotation.ui.FieldValue;
import org.eclipse.ui.services.IDisposable;

class ModelFieldEditingSupport extends EditingSupport implements IDisposable {

	/**
	 * 
	 */
	private final ModelViewer modelViewer;
	private ModelFieldEditorFactory factory;
	public ModelFieldEditingSupport(ModelViewer modelViewer, ColumnViewer viewer, ColumnLabelProvider prov) {
		super(viewer);
		this.modelViewer = modelViewer;
		factory = new ModelFieldEditorFactory(prov);
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return factory.createEditor((FieldValue)element, this.modelViewer.getTable());
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		return ((FieldValue)element).get();
	}

	@Override
	protected void setValue(Object element, Object value) {
		try {
			FieldValue field = (FieldValue)element;
			field.set(value); // Changes model value, getModel() will now return a model with the value changed.
			this.modelViewer.refresh();
			this.modelViewer.setSelection(new StructuredSelection(field));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void dispose() {
		factory.dispose();
	}
}