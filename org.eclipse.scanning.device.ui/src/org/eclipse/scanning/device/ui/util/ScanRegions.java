package org.eclipse.scanning.device.ui.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.ColorConstants;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.scanning.api.points.models.ScanRegion;

/**
 * 
 * Utility for managing scan regions.
 * 
 * @author Matthew Gerring
 *
 */
public class ScanRegions {

	/**
	 * Create the plotted regions for this list of ScanRegions.
	 * @param system
	 * @param regions
	 * @throws Exception
	 */
	public static void createRegions(IPlottingSystem<?> system, List<ScanRegion<IROI>> regions) throws Exception {
		
		if (regions!=null && !regions.isEmpty()) {
			for (ScanRegion<IROI> scanRegion : regions) {
				IRegion region = createRegion(system, (RegionType)scanRegion.getType(), system.getPlotName(), scanRegion.getRoi());
				region.setUserObject(scanRegion); // Override default because we know it.
			}
		}
	}

	/**
	 * 
	 * @param system
	 * @param regionType
	 * @param regionViewName
	 * @param roi - may be null. If null then the region is not added and the plotting UI is left in a state awaiting a drag event.
	 * @return
	 * @throws Exception
	 */
	public static IRegion createRegion(IPlottingSystem<?> system, RegionType regionType, final String regionViewName, IROI roi) throws Exception {
		
		if (system==null) return null;
		IRegion region = system.createRegion(RegionUtils.getUniqueName("Scan "+regionType.getName(), system), regionType);

		String x = system.getSelectedXAxis().getTitle();
		String y = system.getSelectedYAxis().getTitle();
		region.setUserObject(new ScanRegion<IROI>(region.getName(), regionType, Arrays.asList(x,y))); 
		region.setRegionColor(ColorConstants.blue);
		region.setAlpha(25);
		region.setLineWidth(1);
		if (roi!=null) {
			region.setROI(roi);
			system.addRegion(region);
			region.repaint();
		}
		return region;
	}

	/**
	 * Search for and return the regions which are to be involved in a scan.
	 * 
	 * @param system
	 * @return
	 */
	public static List<ScanRegion<IROI>> getScanRegions(IPlottingSystem<?> system) {
		
		final Collection<IRegion> regions = system.getRegions();
		if (regions==null || regions.isEmpty()) return null;
		
		final List<ScanRegion<IROI>> ret = new ArrayList<ScanRegion<IROI>>();
		for (IRegion region : regions) {
			if (region.getUserObject() instanceof ScanRegion) {
				ScanRegion<IROI> sr = (ScanRegion<IROI>)region.getUserObject();
				sr.setRoi(region.getROI());
				ret.add(sr);
			}
		}
		if (ret.isEmpty()) return null;
		return ret;
	}

}
