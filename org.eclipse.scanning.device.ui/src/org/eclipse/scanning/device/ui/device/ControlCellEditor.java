package org.eclipse.scanning.device.ui.device;

import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.richbeans.widgets.decorator.FloatDecorator;
import org.eclipse.richbeans.widgets.decorator.IValueChangeListener;
import org.eclipse.richbeans.widgets.decorator.ValueChangeEvent;
import org.eclipse.richbeans.widgets.internal.GridUtils;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.DevicePreferenceConstants;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolTip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlCellEditor extends CellEditor {
	
	private static final Logger logger = LoggerFactory.getLogger(ControlCellEditor.class);

	// Data
	private org.eclipse.scanning.api.scan.ui.ControlNode     value;
	
	// UI
	private FloatDecorator decorator, incDeco;
	private Text text;

	// Hardware
	private IScannableDeviceService cservice;
	private IScannable<Number>      scannable; // Transient depending on which scannable we are editing.

	private ToolTip tip;


	public ControlCellEditor(Composite parent) {
		super(parent);
		try {
			this.cservice = ServiceHolder.getEventService().createRemoteService(new URI(Activator.getJmsUri()), IScannableDeviceService.class);
		} catch (EventException | URISyntaxException e) {
			logger.error("Cannot get the scannable service!", e);
		}
	}
	
	@Override
	protected Control createControl(Composite parent) {

        final Composite content = new Composite(parent, SWT.NONE);
        content.setBackground(content.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        content.setLayout(new GridLayout(5, false));
        GridUtils.removeMargins(content);
 
		this.text = new Text(content, SWT.LEFT);
        text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
        this.decorator = new FloatDecorator(text);
        this.tip = new ToolTip(text.getShell(), SWT.BALLOON);
        tip.setMessage("Press enter to set the value or use the up and down arrows.");
       
        final Composite buttons = new Composite(content, SWT.NONE);
        buttons.setBackground(content.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        GridData layout = new GridData(SWT.FILL, SWT.FILL, false, false);
        layout.heightHint = 30;
        buttons.setLayoutData(layout);
        buttons.setLayout(new GridLayout(1, false));
        GridUtils.removeMargins(buttons);
        
        final Button up = new Button(buttons, SWT.UP);
        up.setBackground(content.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        layout = new GridData(SWT.FILL, SWT.TOP, false, false);
        layout.heightHint = 15;
        up.setLayoutData(layout);
        up.setImage(Activator.getImageDescriptor("icons/up.png").createImage());
        up.setToolTipText("Nudge value up by increment amount");
     
        final Button down = new Button(buttons, SWT.DOWN);
        down.setBackground(content.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        down.setLayoutData(layout);
        down.setImage(Activator.getImageDescriptor("icons/down.png").createImage());
        down.setToolTipText("Nudge value down by increment amount");

        Text increment = new Text(content, SWT.RIGHT);
        layout = new GridData(SWT.FILL, SWT.CENTER, false, false);
        layout.widthHint = 50;
        increment.setLayoutData(layout);
        this.incDeco = new FloatDecorator(increment);
        incDeco.setMaximum(100);
        incDeco.setMinimum(0);
        incDeco.addValueChangeListener(new IValueChangeListener() {	
			@Override
			public void valueValidating(ValueChangeEvent evt) {
				value.setIncrement(evt.getValue().doubleValue());
			}
		});
        
        final Button stop = new Button(content, SWT.DOWN);
        stop.setBackground(content.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        stop.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
        stop.setImage(Activator.getImageDescriptor("icons/cross-button.png").createImage());
        stop.setToolTipText("Stop current move");

		return content;
	}

	@Override
	protected Object doGetValue() {
		if (tip!=null) tip.setVisible(false);
		return value;
	}

	@Override
	protected void doSetFocus() {
		text.setFocus();
		text.setSelection(text.getText().length());
		
		if (Activator.getDefault().getPreferenceStore().getBoolean(DevicePreferenceConstants.SHOW_CONTROL_TOOLTIPS)) {
			PointerInfo a = MouseInfo.getPointerInfo();
			java.awt.Point loc = a.getLocation();
			
			tip.setLocation(loc.x, loc.y+20);
	        tip.setVisible(true);
		}
	}

	@Override
	protected void doSetValue(Object v) {
		if (v == null) return;
		this.value = (org.eclipse.scanning.api.scan.ui.ControlNode)v;
		try {
			text.setEnabled(true);
			this.scannable = cservice.getScannable(value.getName());
			this.decorator.setMaximum(scannable.getMaximum());
			this.decorator.setMinimum(scannable.getMinimum());
			this.decorator.setValue(scannable.getPosition());
			this.incDeco.setValue(value.getIncrement());
		} catch (Exception e) {
			logger.error("Cannot get scannable!", e);
			text.setEnabled(false);
			text.setText(e.toString());
		}
	}

}
