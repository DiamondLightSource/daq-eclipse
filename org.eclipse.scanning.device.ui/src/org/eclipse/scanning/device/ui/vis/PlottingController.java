package org.eclipse.scanning.device.ui.vis;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.IRegionSystem;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.PointStyle;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scanning.api.points.models.IBoundingBoxModel;
import org.eclipse.scanning.api.points.models.IBoundingLineModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.device.ui.points.ScanRegionProvider;
import org.eclipse.swt.graphics.Color;

/**
 * 
 * This controller can be used by any plotting
 * system to make it process maps. When regions are
 * selected or moved, the scan path is replotted using a job.
 * {@link org.dawnsci.plotting.tools.profile.ProfileTool.ProfileJob}
 * 
 * @author Matthew Gerring
 * @author James Mudd
 * @author Colin Palmer
 *
 */
public class PlottingController implements ISelectionProvider, IAdaptable {
	
	private static final String MAPPING_PATH_NAME = "Mapping Scan Path";
	
	// UI
	private   IPlottingSystem<?> system;
	private   final Color        scanPathColour;
	
	// Data
	private   volatile Object    model; // We make accessing the model single threaded because worker threads are involved.
	
	// Events
	private   IRegionListener    regionListener;
	private   IROIListener       roiListener;
	
	// Workers
	private PathInfoCalculatorJob job;

	public PlottingController(IPlottingSystem<?> system) {
		
		this.system         = system;
		this.listeners      = new HashSet<ISelectionChangedListener>(11);
		this.job            = new PathInfoCalculatorJob(this);
		this.scanPathColour = new Color(null, 160, 32, 240); // purple

		roiListener    = new IROIListener.Stub() {
			@Override
			public void roiDragged(ROIEvent evt) {
				ILineTrace pathTrace = (ILineTrace)system.getTrace(MAPPING_PATH_NAME);
				if (pathTrace!=null) pathTrace.setVisible(false);
				fireRegion((IRegion)evt.getSource(), evt.getROI(), false);
			}
			@Override
			public void update(ROIEvent evt) {
				fireRegion((IRegion)evt.getSource(), evt.getROI(), true);
			}
		};
		regionListener = new IRegionListener.Stub() {

			@Override
			public void regionAdded(RegionEvent evt) {
				evt.getRegion().addROIListener(roiListener);
				fireRegion(evt.getRegion(), evt.getRegion().getROI(), true);
			}
			@Override
			public void regionRemoved(RegionEvent evt) {
				evt.getRegion().removeROIListener(roiListener);
				fireRegion((IRegion)evt.getSource(), evt.getRegion().getROI(), true);
			}
			@Override
			public void regionsRemoved(RegionEvent evt) {
				for (IRegion region : evt.getRegions()) {
					region.removeROIListener(roiListener);
				}
				fireRegion((IRegion)evt.getSource(), evt.getRegion().getROI(), true);
			}
		};
	}

	void plot(PathInfo info) {
		
		boolean newTrace = false;
		//Remove the previous trace
		ILineTrace pathTrace = (ILineTrace)system.getTrace(MAPPING_PATH_NAME);
		
		// If there are no scan regions at all, no trace to draw
		// If the model is not one we draw scan paths for, no trace to draw.
		if (!isScanPathModel() || ScanRegionProvider.getScanRegions(system)==null) {
			if (pathTrace!=null) pathTrace.setVisible(false);
			return;
		}	
		
		if (pathTrace == null) {
			pathTrace = system.createLineTrace(MAPPING_PATH_NAME);
			pathTrace.setTraceColor(scanPathColour);
			pathTrace.setPointStyle(PointStyle.SQUARE);
			newTrace = true;
		}

		// Check if the scan region is currently plotted - if not, we don't want to plot the path either
		// (This fixes a synchronisation bug where the path is added while the scan region drawing event is still
		// active, cancelling the event and making it impossible to draw regions)
		if (info != null) {

			// Get the point coordinates from the last path info and add them to the trace
			pathTrace.setData(info.getX(), info.getY());
			if (newTrace) system.addTrace(pathTrace);
			pathTrace.setVisible(true);
			system.setPlotType(PlotType.IMAGE);
			system.setShowLegend(false);
		}
	}


	private void fireRegion(IRegion region, IROI roi, boolean drawPath) {

		if (region==null) return;
		
		if (!(region.getUserObject() instanceof ScanRegion)) return; // Must be another region.
		roi.setName(region.getName());
		setSelection(new StructuredSelection(roi));
		
		List<ScanRegion<IROI>> sregions = ScanRegionProvider.getScanRegions(system);
		if (drawPath) {
			if (sregions==null) {
				setPathVisible(false);
			} else {
				job.schedule(model, sregions);
			}
		}
	}

	
	private Set<ISelectionChangedListener> listeners;
	private ISelection currentSelection;
	
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	@Override
	public ISelection getSelection() {
		return currentSelection;
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Method calls listener in background thread mto make frequent updates possible.
	 */
	@Override
	public void setSelection(ISelection selection) {
		
		if (listeners.isEmpty()) return;
		currentSelection = selection;
		SelectionChangedEvent e = new SelectionChangedEvent(this, currentSelection);
		ISelectionChangedListener[] sl = listeners.toArray(new ISelectionChangedListener[listeners.size()]);
		for (ISelectionChangedListener s : sl) s.selectionChanged(e);
	}

	public void clear() {
		if (listeners!=null) listeners.clear();
	}

	public void setModel(Object model) throws Exception {
		
		this.model = model;
		if (isScanPathModel()) {			
			setRegionsVisible(true);
			job.schedule(model, ScanRegionProvider.getScanRegions(system));
		} else {
			setPathVisible(false);
			setRegionsVisible(false);
		}
	}
	
	private boolean isScanPathModel() {
		// TODO We may want to change the definition of this.
		return model instanceof IBoundingBoxModel || model instanceof IBoundingLineModel;
	}

	private void setRegionsVisible(boolean vis) {
		Collection<IRegion> regions = system.getRegions();
		for (IRegion iRegion : regions) {
			if (iRegion.getUserObject() instanceof ScanRegion) iRegion.setVisible(vis);
		}
	}

	void setPathVisible(boolean vis) {
		ILineTrace pathTrace = (ILineTrace)system.getTrace(MAPPING_PATH_NAME);
        if (pathTrace!=null) pathTrace.setVisible(vis);
	}

	public void dispose() {
		clear();
		if (system!=null) {
		    system.removeRegionListener(regionListener);
		    for (IRegion region : system.getRegions()) {
		    	if (region.getUserObject() instanceof ScanRegion) region.removeROIListener(roiListener);
			}
			system.dispose();
		}
		scanPathColour.dispose();
	}

	public void connect() {
	    for (IRegion region : system.getRegions()) {
	    	if (region.getUserObject() instanceof ScanRegion) region.addROIListener(roiListener);
		}
        system.addRegionListener(regionListener);
	}


	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (PlottingController.class == adapter) return (T)this;
		if (IPlottingSystem.class == adapter) return (T)system;
		if (IRegionSystem.class == adapter)   return (T)system;
		if (IToolPageSystem.class == adapter) return system.getAdapter(adapter);
		return null;
	}

	public IImageTrace getImageTrace() {
		IImageTrace it = (IImageTrace)system.getTraces(IImageTrace.class).iterator().next();
		return it;
	}

}
