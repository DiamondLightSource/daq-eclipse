package org.eclipse.scanning.device.ui.points;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.device.ui.util.ScanRegions;

class ScanRegionContentProvider implements IStructuredContentProvider {

	
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
		List<ScanRegion<IROI>> regions = ScanRegions.getScanRegions(system);
		if (regions==null) return new ScanRegion[]{new ScanRegion<IROI>()};
		return regions.toArray(new ScanRegion[regions.size()]);
	}

}
