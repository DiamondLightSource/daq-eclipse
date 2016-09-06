package org.eclipse.scanning.device.ui.util;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.DevicePreferenceConstants;

public class BoxConvert {
	
	private IImageTrace trace;

	/**
	 * 
	 * @param trace
	 * @throws ClassCastException if trace is not an IImageTrace
	 */
	public BoxConvert(ITrace trace) {
		this.trace = (IImageTrace)trace;
		Activator.getDefault().getPreferenceStore().setDefault(DevicePreferenceConstants.NUMBER_FORMAT, "##########0.0###");
	}

	public IROI toROI(BoundingBox box) throws Exception {
		
		double[] start = trace.getPointInImageCoordinates(new double[]{box.getFastAxisStart(), box.getSlowAxisStart()});
		double[] end   = trace.getPointInImageCoordinates(new double[]{box.getFastAxisEnd(), box.getSlowAxisEnd()});
		
		RectangularROI ret = new RectangularROI(start, end);
		ret.setPlot(true);
		return ret;
	}
	
	public BoundingBox toBox(IROI roi) throws Exception {
		
		IRectangularROI bounds = roi.getBounds();
		double[] start = trace.getPointInAxisCoordinates(bounds.getPoint());
		double[] end   = trace.getPointInAxisCoordinates(bounds.getEndPoint());
		
		BoundingBox box = new BoundingBox(start, end);
		box.setRegionName(roi.getName());
		box.setNumberFormat(Activator.getDefault().getPreferenceStore().getString(DevicePreferenceConstants.NUMBER_FORMAT));
		return box;
	}

}
