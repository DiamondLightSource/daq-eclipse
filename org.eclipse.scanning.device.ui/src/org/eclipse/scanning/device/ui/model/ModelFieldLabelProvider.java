package org.eclipse.scanning.device.ui.model;

import java.net.URI;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;
import org.eclipse.scanning.api.annotation.ui.FieldValue;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.eclipse.scanning.device.ui.util.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ModelFieldLabelProvider extends EnableIfColumnLabelProvider {

	private Image ticked;
	private Image unticked;
	
	private static final Logger logger = LoggerFactory.getLogger(ModelFieldLabelProvider.class);
	
	private IScannableDeviceService cservice;
	private final ModelViewer       viewer;
	
	public ModelFieldLabelProvider(ModelViewer viewer) {
		this.viewer = viewer;
		try {
			cservice = ServiceHolder.getEventService().createRemoteService(new URI(Activator.getJmsUri()), IScannableDeviceService.class);
		} catch (Exception e) {
			logger.error("Unable to make a remote connection to "+IScannableDeviceService.class.getSimpleName());
		}
	}
	
	public Color getForeground(Object ofield) {
		Color ret = super.getForeground(ofield);
		if (ret!=null) return ret;
		if (ofield instanceof FieldValue && viewer.isValidationError((FieldValue)ofield)) {
			return Display.getDefault().getSystemColor(SWT.COLOR_RED);
		} else {
			return null;
		}
	}


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
		
		buf.append(getUnit(field));
		return buf.toString();
	}
	
	private String getUnit(FieldValue field) {
		
		FieldDescriptor anot = field.getAnnotation();
		if (anot!=null) {
			if (anot.scannable().length()>0 && cservice !=null) {
				try {
				    String scannableName = (String)FieldValue.get(field.getModel(), anot.scannable());
				    
				    if (scannableName!=null && scannableName.length()>0) {
					    IScannable<?> scannable = cservice.getScannable(scannableName);
					    
					    String unit = scannable.getUnit();
					    if (unit!=null && unit.length()>0) {
					        return " "+scannable.getUnit();
					    }
				    }
				    
				} catch (Exception ne) {
					ne.printStackTrace();
				}
			}
			
			if (anot.unit().length()>0) return " "+anot.unit();
		}
		return "";
	}

	public void dispose() {
		if (ticked!=null)   ticked.dispose();
		if (unticked!=null) unticked.dispose();
		super.dispose();
	}

}
