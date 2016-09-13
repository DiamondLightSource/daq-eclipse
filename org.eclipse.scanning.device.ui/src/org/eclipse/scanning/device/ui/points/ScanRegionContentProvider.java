package org.eclipse.scanning.device.ui.points;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scanning.api.points.models.ScanRegion;

public class ScanRegionContentProvider implements IStructuredContentProvider {

	
	private IPlottingSystem<?> system;

	@Override
	public void dispose() {
		system = null;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.system = (IPlottingSystem<?>)newInput;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		List<ScanRegion<IROI>> regions = getScanRegions(system);
		if (regions==null) return new ScanRegion[]{new ScanRegion<IROI>()};
		return regions.toArray(new ScanRegion[regions.size()]);
	}

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
