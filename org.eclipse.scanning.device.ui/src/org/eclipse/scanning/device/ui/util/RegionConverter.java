package org.eclipse.scanning.device.ui.util;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.DevicePreferenceConstants;

public class RegionConverter {
	
	private IImageTrace trace;
	private IROI        roi;

	/**
	 * 
	 * @param trace
	 * @throws ClassCastException if trace is not an IImageTrace
	 */
	public RegionConverter(ITrace trace, IROI roi) {
		this.trace = (IImageTrace)trace;
		this.roi   = roi;
		Activator.getDefault().getPreferenceStore().setDefault(DevicePreferenceConstants.NUMBER_FORMAT, "##########0.0###");
	}
	
	public BoundingBox getBoundingBox() throws Exception {
		
		IRectangularROI bounds = roi.getBounds();
		double[] start = trace.getPointInAxisCoordinates(bounds.getPoint());
		double[] end   = trace.getPointInAxisCoordinates(bounds.getEndPoint());
		
		BoundingBox box = new BoundingBox(start, end);
		box.setRegionName(roi.getName());
		box.setNumberFormat(Activator.getDefault().getPreferenceStore().getString(DevicePreferenceConstants.NUMBER_FORMAT));
		return box;
	}

}
