package org.eclipse.scanning.device.ui.device;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlValueJob extends Job {
	
	private static Logger logger = LoggerFactory.getLogger(ControlValueJob.class);

	private Text               text;
	private IScannable<Number> scannable;
	private Number             value;
	

	public ControlValueJob(Text text) {
		super("Set Value");
		this.text = text;
	}
	

	public void setPosition(IScannable<Number> scannable, Number value) {
		cancel();
		this.scannable = scannable;
		this.value     = value;
		schedule();
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			setEnabled(false);
		    scannable.setPosition(value); // Blocking call
		    return Status.OK_STATUS;
		    
		} catch (Exception e) {
			setText(e.getMessage());
			logger.error("Cannot set position!", e);
		    return Status.CANCEL_STATUS;
			
		} finally {
			setEnabled(true);
		}
	}


	private void setText(final String message) {
		text.getDisplay().asyncExec(new Runnable() {
			public void run() {
				text.setText(message);
			}
		});
	}

	private void setEnabled(final boolean b) {
		text.getDisplay().asyncExec(new Runnable() {
			public void run() {
				text.setEditable(b);
				text.setBackground(text.getDisplay().getSystemColor(SWT.COLOR_WHITE));
				text.setEnabled(b);
			}
		});
	}

}
