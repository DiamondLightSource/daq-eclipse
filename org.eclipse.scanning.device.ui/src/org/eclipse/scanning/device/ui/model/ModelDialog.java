package org.eclipse.scanning.device.ui.model;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelDialog<T> extends Dialog {

	private static final Logger logger = LoggerFactory.getLogger(ModelDialog.class);
	
	private ModelViewer<T>   modelEditor;

	private String preamble;
	
	public ModelDialog(Shell parentShell) {
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
			if (preamble!=null) {
				Label label = new Label(main, SWT.WRAP);
				label.setText(preamble);
				label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			}
			modelEditor = new ModelViewer<T>();
			Control created = modelEditor.createPartControl(main);
			created.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		} catch (Exception e) {
			logger.error("Cannot create model viewer!", e);
		}
		
		return main;

	}

	public T getModel() {
		return modelEditor.getModel();
	}

	public void setModel(T model) throws InstantiationException, IllegalAccessException {
		modelEditor.setModel(model);
	}

	public void setPreamble(String message) {
		this.preamble = message;
	}

}
