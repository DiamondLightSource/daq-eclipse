package org.eclipse.scanning.device.ui.device;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scanning.api.IScannable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlValueJob extends Job {
	
	private static Logger logger = LoggerFactory.getLogger(ControlValueJob.class);

	
	// Data
	private IScannable<Number> scannable;
	private Number             value;

    // UI
	private ControlCellEditor editor;


	public ControlValueJob(ControlCellEditor editor) { // TODO Float spinner?
		super("Set Value");
		this.editor = editor;
	}
	

	public void setPosition(IScannable<Number> scannable, Number value) {
		cancel();
		this.scannable = scannable;
		this.value     = value;
		setName("Set '"+scannable.getName()+"' to "+value);
		schedule();
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			editor.setSafeEnabled(false);
		    scannable.setPosition(value); // Blocking call
		    return Status.OK_STATUS;
		    
		} catch (Exception e) {
			editor.setSafeText(e.getMessage());
			logger.error("Cannot set position!", e);
		    return Status.CANCEL_STATUS;
			
		} finally {
			editor.setSafeEnabled(true);
		}
	}

}
