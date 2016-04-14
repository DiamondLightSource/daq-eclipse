package org.eclipse.scanning.example.xcen.ui.handlers;

import org.eclipse.richbeans.api.generator.IGuiGeneratorService;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.ui.IModifyHandler;
import org.eclipse.scanning.example.xcen.beans.XcenBean;

public class ModifyHandler extends XcenHandler implements IModifyHandler<XcenBean> {
	
	private static IGuiGeneratorService guiservice;

	@Override
	public boolean modify(XcenBean bean) throws Exception {
		
		BeamPosition pos = new BeamPosition(bean.getX(), bean.getY());
		// TODO FIXME Require method to edit 
		//pos = guiservice.openDialog(pos);
		//if (pos == null) return true; // We still looked at it
	
		ISubmitter<XcenBean> submitter = eventService.createSubmitter(conf.getUri(), conf.getSubmissionQueue());
		submitter.replace(bean);
		
		return true; // It was dealt with 
	}

	public static IGuiGeneratorService getGuiservice() {
		return guiservice;
	}

	public static void setGuiservice(IGuiGeneratorService guiservice) {
		ModifyHandler.guiservice = guiservice;
	}

}
