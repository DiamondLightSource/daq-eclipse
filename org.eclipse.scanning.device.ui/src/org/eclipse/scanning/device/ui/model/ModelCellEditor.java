package org.eclipse.scanning.device.ui.model;

import java.awt.MouseInfo;
import java.awt.PointerInfo;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.ColorConstants;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.IRegionSystem;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.richbeans.widgets.internal.GridUtils;
import org.eclipse.scanning.api.annotation.ui.FieldValue;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.util.PlotUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolTip;
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
		super();
		this.value     = value;
		this.labelProv = labelProv;
		create(parent);
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
	
	@Override
    protected Control createContents(Composite ancestor) {
    	
    	try {
	    	Class<?> ovalue = value.getType();
	    	if (BoundingBox.class.isAssignableFrom(ovalue)) {
	    		
	    		final Composite parent = new Composite(ancestor, SWT.NONE);
	    		parent.setLayout(new GridLayout(2, false));
	    		GridUtils.removeMargins(parent);
	            Control content = super.createContents(parent);
	            content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    		
	    		String regionViewName = PlotUtil.getRegionViewName();
	    		if (regionViewName!=null) {
	    			final ToolTip tip = new ToolTip(parent.getShell(), SWT.BALLOON);
		            Button roi = new Button(parent, SWT.DOWN);
		            GridData layout = new GridData(SWT.FILL, SWT.TOP, false, false);
		            layout.heightHint = 23;
		            roi.setLayoutData(layout);
		            
		            roi.setToolTipText("Press to click and drag a box on '"+PlotUtil.getRegionViewName()+"'");
		            roi.setImage(Activator.getImageDescriptor("icons/ProfileBox.png").createImage());
		            roi.addSelectionListener(new SelectionAdapter() {
		            	public void widgetSelected(SelectionEvent e) {
		            		IRegionSystem system = PlotUtil.getRegionSystem();
		            		if (system!=null) {
			            		BoundingBox existing = getExistingValue(value);
			            		if (existing==null || system.getRegion(existing.getRegionName())==null) {
									try {
										system.createRegion(RegionUtils.getUniqueName("boundingBox", system), RegionType.BOX);
										showTip(tip, "Drag a box in the '"+regionViewName+"' to create a bounding box.");
										system.addRegionListener(new IRegionListener.Stub() {
											@Override
											public void regionCancelled(RegionEvent evt) {
												system.removeRegionListener(this);
											}
										    @Override
											public void regionAdded(RegionEvent evt) {
												system.removeRegionListener(this);
												IRegion region = evt.getRegion();
												if (region==null) return;
												region.setUserObject(BoundingBox.MARKER.BOX);
												region.setRegionColor(ColorConstants.blue);
												region.setAlpha(10);
												region.setLineWidth(1);
											}
										});
										
									} catch (Exception e1) {
										logger.error("Cannot create a bounding box!", e1);
										return;
									}
			            		} else if (existing!=null && system.getRegion(existing.getRegionName())!=null) {
			            			((IPlottingSystem<?>)system).setFocus();
									showTip(tip, "The region '"+existing.getRegionName()+"' exists, drag it to change the bounding box.");
			            		}
		            		}
		            	}
		            });
	    		}
	    		return parent;
	    	}
    	} catch (Exception e1) {
			logger.error("Cannot get type of field "+value, e1);
    	}
        return super.createContents(ancestor);
    }

	protected void showTip(ToolTip tip, String message) {
    	tip.setMessage(message);
		PointerInfo a = MouseInfo.getPointerInfo();
		java.awt.Point loc = a.getLocation();
		
		tip.setLocation(loc.x, loc.y+20);
        tip.setVisible(true);
	}

	protected BoundingBox getExistingValue(FieldValue value2) {
		try {
			return (BoundingBox)value2.get();
		} catch (Exception ne) {
			return null;
		}
	}

	protected void updateContents(Object value) {
		if ( getDefaultLabel() == null) {
			return;
		}
		if (value == null ) return;
		getDefaultLabel().setText(labelProv.getText(value));
	}

}
