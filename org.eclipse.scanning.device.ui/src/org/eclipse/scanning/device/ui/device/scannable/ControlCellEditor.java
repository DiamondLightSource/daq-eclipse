package org.eclipse.scanning.device.ui.device;

import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.richbeans.widgets.decorator.BoundsDecorator;
import org.eclipse.richbeans.widgets.decorator.FloatDecorator;
import org.eclipse.richbeans.widgets.decorator.IValueChangeListener;
import org.eclipse.richbeans.widgets.decorator.ValueChangeEvent;
import org.eclipse.richbeans.widgets.internal.GridUtils;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.ITerminatable;
import org.eclipse.scanning.api.ITerminatable.TerminationPreference;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListenable;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.ui.ControlNode;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.DevicePreferenceConstants;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolTip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlCellEditor extends CellEditor implements IPositionListener {
	
	private static final Logger logger = LoggerFactory.getLogger(ControlCellEditor.class);

	// Data
	private ControlNode     node;
	
	// UI
	private BoundsDecorator decorator, incDeco;
	private Text            text;
	private ToolTip         tip;
	private Button          stop;
	private Button          up, down;

	// Hardware
	private IScannableDeviceService cservice;
	private IScannable<Number>      scannable; // Transient depending on which scannable we are editing.	
	private ControlValueJob         job;

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
        this.decorator = new FloatDecorator(text, Activator.getDefault().getPreferenceStore().getString(DevicePreferenceConstants.NUMBER_FORMAT));
        this.tip = new ToolTip(text.getShell(), SWT.BALLOON);
        tip.setMessage("Press enter to set the node or use the up and down arrows.");
        text.addListener(SWT.Traverse, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (event.detail == SWT.TRAVERSE_RETURN) {
                    setPosition(decorator.getValue());
                }
            }
        });
        
        final Composite buttons = new Composite(content, SWT.NONE);
        buttons.setBackground(content.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        GridData layout = new GridData(SWT.FILL, SWT.FILL, false, false);
        layout.heightHint = 30;
        buttons.setLayoutData(layout);
        buttons.setLayout(new GridLayout(1, false));
        GridUtils.removeMargins(buttons);
        
        this.up = new Button(buttons, SWT.UP);
        up.setBackground(content.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        layout = new GridData(SWT.FILL, SWT.TOP, false, false);
        layout.heightHint = 15;
        up.setLayoutData(layout);
        up.setImage(Activator.getImageDescriptor("icons/up.png").createImage());
        up.setToolTipText("Nudge node up by increment amount");
        up.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent e) {
        		nudge(incDeco.getValue());
        	}
        });
     
        this.down = new Button(buttons, SWT.DOWN);
        down.setBackground(content.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        down.setLayoutData(layout);
        down.setImage(Activator.getImageDescriptor("icons/down.png").createImage());
        down.setToolTipText("Nudge node down by increment amount");
        down.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent e) {
        		if (incDeco.getValue()==null) return;
        		nudge(-1*incDeco.getValue().doubleValue());
        	}
        });

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
				node.setIncrement(evt.getValue().doubleValue());
			}
		});
        
        this.stop = new Button(content, SWT.DOWN);
        stop.setBackground(content.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        stop.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
        stop.setImage(Activator.getImageDescriptor("icons/cross-button.png").createImage());
        stop.setToolTipText("Stop current move");
        stop.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent e) {
        		Thread test = new Thread("Test terninate in thread") {
        			public void run() {
        				try {
        					((ITerminatable)scannable).terminate(TerminationPreference.CONTROLLED);
        				} catch (Exception e1) {
        					logger.error("Problem stopping motor!", e1);
        				}
        			}
        		};
        		test.start();
        	}
        });

		job = new ControlValueJob(this);

		return content;
	}

	protected void nudge(Number value) {
		try {
			final Number pos = scannable.getPosition();
			if (pos == null) return;
			double loc = pos.doubleValue()+value.doubleValue();
			if (!decorator.check(loc)) {
				MessageDialog.openError(text.getShell(), "Invalid Value '"+decorator.format(loc)+"'", "The value of '"+decorator.format(loc)+"' is out of bounds for device '"+scannable.getName()+"'");
			    return;
			}
			setPosition(loc);
		} catch (Exception e) {
			logger.error("Cannot nudge value!", e);
		}
	}

	protected void setPosition(Number value) {
		job.setPosition(scannable, value);
	}
	
	/**
	 * Not the SWT thread, events come from the device.
	 */
	@Override
	public void positionChanged(PositionEvent evt) throws ScanningException {
		setPosition(evt, false);
	}
	
	@Override
	public void positionPerformed(PositionEvent evt) throws ScanningException {
		setPosition(evt, true);
	}
	
	private void setPosition(PositionEvent evt, boolean enabled) {
		final double pos = evt.getPosition().getValue(scannable.getName());
		setSafeValue(pos);
		setSafeEnabled(enabled);
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
	protected void focusLost() {
		super.focusLost();
	}
	
	@Override
	protected Object doGetValue() {
		if (tip!=null) tip.setVisible(false);
		if (scannable!=null && scannable instanceof IPositionListenable) {
			((IPositionListenable)scannable).removePositionListener(this);
		}
		return node;
	}
	
	@Override
	protected void doSetValue(Object v) {
		
		if (v == null) return;
		this.node = (ControlNode)v;
		try {
			if (scannable!=null && scannable instanceof IPositionListenable) {
				((IPositionListenable)scannable).removePositionListener(this);
			}
			text.setEnabled(true);
			this.scannable = cservice.getScannable(node.getName());
			stop.setEnabled(scannable instanceof ITerminatable);
			if (scannable!=null && scannable instanceof IPositionListenable) {
				((IPositionListenable)scannable).addPositionListener(this);
			}
			
			this.decorator.setMaximum(scannable.getMaximum());
			this.decorator.setMinimum(scannable.getMinimum());
			this.decorator.setValue(scannable.getPosition());
			this.incDeco.setValue(node.getIncrement());
			
			
		} catch (Exception e) {
			logger.error("Cannot get scannable!", e);
			text.setEnabled(false);
			text.setText(e.toString());
		}
	}


	/**
	 * Thread safe
	 * @param message
	 */
	protected void setSafeValue(double intermeadiatePos) {
		asynch(new Runnable() {
			public void run() {
				decorator.setValue(intermeadiatePos); // This call renders the text and bounds correctly.
			}
		});
	}
	
	/**
	 * Thread safe
	 * @param message
	 */
	protected void setSafeText(final String message) {
		asynch(new Runnable() {
			public void run() {
				text.setText(message);
			}
		});
	}

	/**
	 * Thread safe.
	 * @param b
	 */
	protected void setSafeEnabled(final boolean b) {
		asynch(new Runnable() {
			public void run() {
				text.setEditable(b);
				up.setEnabled(b);
				down.setEnabled(b);
			    text.setBackground(text.getDisplay().getSystemColor(SWT.COLOR_WHITE));
				if (b) {
				    text.setForeground(text.getDisplay().getSystemColor(SWT.COLOR_BLACK));
				} else {
				    text.setForeground(text.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
				}
			}
		});
	}

	private void asynch(Runnable runnable) {
		if (text==null || text.isDisposed()) return;
		text.getDisplay().asyncExec(runnable);
	}

}
