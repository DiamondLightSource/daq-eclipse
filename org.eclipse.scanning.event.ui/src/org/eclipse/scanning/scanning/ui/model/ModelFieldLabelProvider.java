package org.eclipse.scanning.scanning.ui.model;

import org.eclipse.scanning.api.points.annot.FieldDescriptor;
import org.eclipse.scanning.api.points.annot.FieldValue;
import org.eclipse.scanning.event.ui.Activator;
import org.eclipse.scanning.scanning.ui.util.StringUtils;
import org.eclipse.swt.graphics.Image;

class ModelFieldLabelProvider extends EnableIfColumnLabelProvider {

	private Image ticked;
	private Image unticked;

	/**
	 * The <code>LabelProvider</code> implementation of this
	 * <code>ILabelProvider</code> method returns <code>null</code>.
	 * Subclasses may override.
	 */
	public Image getImage(Object ofield) {
		
		if (ofield == null) return null;
		
		FieldValue field  = (FieldValue)ofield;
		Object   element  = field.get();
		if (element instanceof Boolean) {
			if (ticked==null)   ticked   = Activator.getImageDescriptor("icons/ticked.png").createImage();
			if (unticked==null) unticked = Activator.getImageDescriptor("icons/unticked.gif").createImage();
			Boolean val = (Boolean)element;
			return val ? ticked : unticked;
		}
		return null;
	}

	/**
	 * The <code>LabelProvider</code> implementation of this
	 * <code>ILabelProvider</code> method returns the element's
	 * <code>toString</code> string. Subclasses may override.
	 */
	public String getText(Object ofield) {
		
		if (ofield == null)            return "";
		
		FieldValue field  = (FieldValue)ofield;
		Object   element  = field.get();
		if (element == null)            return "";
		if (element instanceof Boolean) return "";
		
		StringBuilder buf = new StringBuilder();
		if (element.getClass().isArray()) {
			buf.append( StringUtils.toString(element) );
		} else {
		    buf.append(element.toString());//$NON-NLS-1$
		}
		
		FieldDescriptor anot = field.getAnnotation();
		if (anot!=null) buf.append(" "+anot.unit());
		return buf.toString();
	}
	
	public void dispose() {
		if (ticked!=null)   ticked.dispose();
		if (unticked!=null) unticked.dispose();
		super.dispose();
	}

}
