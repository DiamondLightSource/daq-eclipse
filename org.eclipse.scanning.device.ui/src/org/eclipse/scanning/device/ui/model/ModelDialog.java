package org.eclipse.scanning.device.ui.model;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.scanning.api.IValidator;
import org.eclipse.scanning.api.IValidatorService;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelDialog extends Dialog {

	private static final Logger logger = LoggerFactory.getLogger(ModelDialog.class);
	
	private ModelViewer   modelEditor;
	
	protected ModelDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
	
	@Override
	public Control createDialogArea(Composite parent) {

		final Composite main = (Composite)super.createDialogArea(parent);
		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		try {
			modelEditor = new ModelViewer();
			Control created = modelEditor.createPartControl(main);
			created.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		} catch (Exception e) {
			logger.error("Cannot create model viewer!", e);
		}
		
		return main;

	}

	public Object getModel() {
		return modelEditor.getModel();
	}

	public void setModel(Object model) throws InstantiationException, IllegalAccessException {
		modelEditor.setModel(model);
	}

}
