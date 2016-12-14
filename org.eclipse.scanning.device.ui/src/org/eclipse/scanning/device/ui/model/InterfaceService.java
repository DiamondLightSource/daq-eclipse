package org.eclipse.scanning.device.ui.model;

import org.eclipse.scanning.api.ui.auto.IInterfaceService;
import org.eclipse.scanning.api.ui.auto.IModelDialog;
import org.eclipse.scanning.api.ui.auto.InterfaceInvalidException;
import org.eclipse.swt.widgets.Shell;

public class InterfaceService implements IInterfaceService {
	
	static {
		System.out.println("Starting "+InterfaceService.class.getSimpleName());
	}

	@Override
	public <T, O> IModelDialog<O> createModelDialog(T shell) throws InterfaceInvalidException {
		if (!(shell instanceof Shell)) throw new InterfaceInvalidException(getClass().getSimpleName()+" can only deal with SWT Shells!");
		return new ModelDialog<>((Shell)shell);
	}

}
