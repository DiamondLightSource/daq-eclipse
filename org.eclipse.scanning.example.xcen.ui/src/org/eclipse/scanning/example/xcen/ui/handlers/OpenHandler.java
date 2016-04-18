package org.eclipse.scanning.example.xcen.ui.handlers;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.scanning.api.ui.IResultHandler;
import org.eclipse.scanning.example.xcen.beans.XcenBean;
import org.eclipse.swt.widgets.Display;

public class OpenHandler extends XcenHandler implements IResultHandler<XcenBean> {

	@Override
	public boolean open(XcenBean bean) throws Exception {
		
	   MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Position '"+bean.getName()+"'", 
                   "The center was [x,y]: "+bean.getX()+","+bean.getY());
	   return true; // We handled it
	}

}
