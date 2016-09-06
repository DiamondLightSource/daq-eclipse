package org.eclipse.scanning.device.ui.util;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.plotting.api.region.IRegionSystem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;

public class PlotUtil {
    
	public static IRegionSystem getRegionSystem() {
    	IViewPart part = getRegionView();
    	if (part !=null) return part.getAdapter(IRegionSystem.class);
    	return null;
    }

	public static String getRegionViewName() {
    	IViewPart part = getRegionView();
    	if (part !=null) return part.getTitle();
    	return null;
    }
    /**
     * 
     * @return the plotting systems, if any, whose parent part respond to getAdapter(IROI.class)
     */
	public static IViewPart getRegionView() {
    	final IViewReference[] views = PageUtil.getPage().getViewReferences();
    	for (IViewReference vr : views) {
			IViewPart part = vr.getView(false);
			if (part==null) continue;
			final IROI roi = (IROI)part.getAdapter(IROI.class);
			if (roi!=null) return part;
		}
    	return null;
    }


}
