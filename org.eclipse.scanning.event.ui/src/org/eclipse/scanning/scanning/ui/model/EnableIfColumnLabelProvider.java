package org.eclipse.scanning.scanning.ui.model;

import org.eclipse.dawnsci.analysis.api.processing.model.ModelField;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

class EnableIfColumnLabelProvider extends ColumnLabelProvider {

	private Font  italic;

	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
	 */
	public Color getForeground(Object ofield) {
		if (ofield == null) return null;

		ModelField field  = (ModelField)ofield;
		if (ModelFieldEditors.isEnabled(field)) {
			return null;
		} else {
			return Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
		}
	}
	public Font getFont(Object ofield) {
		if (ofield == null) return null;

		ModelField field  = (ModelField)ofield;
		if (ModelFieldEditors.isEnabled(field)) {
			return null;
		} else {
			if (italic == null) {
				final FontData shellFd = Display.getDefault().getActiveShell().getFont().getFontData()[0];
				FontData fd = new FontData(shellFd.getName(), shellFd.getHeight(), SWT.ITALIC);
				italic = new Font(null, fd);
			}
			return italic;
		}
	}


	public void dispose() {
		if (italic!=null)   italic.dispose();
        super.dispose();
	}
}
