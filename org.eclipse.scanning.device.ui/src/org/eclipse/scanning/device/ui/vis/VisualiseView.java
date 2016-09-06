package org.eclipse.scanning.device.ui.vis;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.region.ColorConstants;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.IRegionSystem;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Random;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scanning.api.annotation.ui.FieldValue;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.IBoundingBoxModel;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.eclipse.scanning.device.ui.points.GeneratorDescriptor;
import org.eclipse.scanning.device.ui.util.BoxConvert;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A view which sends IROI events, implements IROI.class in getAdapter(...) and 
 * responds to model selections which contain rois.
 * 
 * @author Matthew Gerring
 *
 */
public class VisualiseView extends ViewPart implements IAdaptable, ISelectionListener {
	
	public static final String ID = "org.eclipse.scanning.device.ui.vis.visualiseView";
	
	private static final Logger logger = LoggerFactory.getLogger(VisualiseView.class);
	
	// UI
	protected IPlottingSystem<Composite>     system;
	protected IROI                           currentROI;
	
	// Events
	private   IRegionListener    regionListener;
	private   IROIListener       roiListener;
	private   ISelectionProvider selectionProvider;


	public VisualiseView() {
		try {
			IPlottingService service = ServiceHolder.getPlottingService();
			system = service.createPlottingSystem();
			
			roiListener    = new IROIListener.Stub() {
				@Override
				public void update(ROIEvent evt) {
					if (selectionProvider!=null) {
						fireRegion((IRegion)evt.getSource(), evt.getROI());
					}
				}
			};
			regionListener = new IRegionListener.Stub() {

				@Override
				public void regionAdded(RegionEvent evt) {
					evt.getRegion().addROIListener(roiListener);
					fireRegion(evt.getRegion(), evt.getRegion().getROI());
				}
				@Override
				public void regionRemoved(RegionEvent evt) {
					evt.getRegion().removeROIListener(roiListener);
				}
				@Override
				public void regionsRemoved(RegionEvent evt) {
					for (IRegion region : evt.getRegions()) {
						region.removeROIListener(roiListener);
					}
				}
				
			};

		} catch (Exception ne) {
			logger.error("Unable to make plotting system", ne);
			system = null; // It creates the view but there will be no plotting system 
		}

	}

	protected void fireRegion(IRegion region, IROI roi) {
		currentROI = roi;
		if (region!=null && currentROI!=null) currentROI.setName(region.getName());
		if (currentROI==null) return;
		selectionProvider.setSelection(new StructuredSelection(currentROI));
	}

	@Override
	public void createPartControl(Composite parent) {

		system.createPlotPart(parent, getPartName(), getViewSite().getActionBars(), PlotType.IMAGE, this);  

		// Plot a random image
		IDataset x = DatasetFactory.createRange(-10d, 10d, 20d/3012, Dataset.FLOAT);
		IDataset y = DatasetFactory.createRange(100d, 200d, 20d/4096, Dataset.FLOAT);
		system.createPlot2D(Random.rand(4096, 3012), Arrays.asList(new IDataset[]{x,y}), null);
		
	    for (IRegion region : system.getRegions()) {
	    	region.addROIListener(roiListener);
		}
        system.addRegionListener(regionListener);
        
        this.selectionProvider = new SelectionProvider();
        getSite().setSelectionProvider(selectionProvider);
        
        getSite().getPage().addSelectionListener(this);
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection==null) return;
		if (!(selection instanceof StructuredSelection)) return;
		final Object object = ((StructuredSelection)selection).getFirstElement();
		if (object==null) return;
		if (object instanceof FieldValue) {
			processFieldValue((FieldValue)object);
		} else if (object instanceof GeneratorDescriptor<?>) {
			processGenerator((GeneratorDescriptor<?>)object);
		}
	}
	
	private void processGenerator(GeneratorDescriptor<?> gen) {
	
		try {
			if (gen.getModel() instanceof IBoundingBoxModel) {
				setRegionPosition(((IBoundingBoxModel)gen.getModel()).getBoundingBox());
			} else { // Cannot deal with regions
				setRegionPosition(null);
			}
		} catch (Exception ignored) {
			logger.trace("Unable to deal with generator "+gen, ignored);
		}
	}
	
	private void processFieldValue(FieldValue value) {
		try {
	    	Class<?> ovalue = value.getType();
	    	if (BoundingBox.class.isAssignableFrom(ovalue)) {
	    		BoundingBox box = (BoundingBox)value.get();
	    		if (box==null) return;
	    		
	    		setRegionPosition(box);
 	    	}
		} catch (Exception ignored) {
			logger.trace("Cannot get type of field!", ignored);
		}
	}

	private void setRegionPosition(BoundingBox box) throws Exception {
		
		if (box==null) {
			
			Collection<IRegion> regions = system.getRegions();
			for (IRegion iRegion : regions) {
				if (iRegion.getUserObject()==BoundingBox.MARKER.BOX) system.removeRegion(iRegion);
			}
			
		} else {
		
			BoxConvert converter = new BoxConvert(system.getTraces(IImageTrace.class).iterator().next());
			IROI roi = converter.toROI(box);
			
		    String regionName = box.getRegionName();
		    IRegion region = system.getRegion(regionName);
		    if (region==null) region = createRegion(box);
		    
	    	region.setROI(roi);
		}
	}

	private IRegion createRegion(BoundingBox box) throws Exception {
		
		String regionName = RegionUtils.getUniqueName("boundingBox", system);
		IRegion region = system.createRegion(regionName, RegionType.BOX);
		region.setUserObject(BoundingBox.MARKER.BOX);
		region.setRegionColor(ColorConstants.blue);
		region.setAlpha(10);
		region.setLineWidth(1);
     	system.addRegion(region);
    	box.setRegionName(regionName);
    	return region;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter){
		if (IROI.class == adapter)            return currentROI!=null ? (T)currentROI : (T)(new RectangularROI());
		if (IPlottingSystem.class == adapter) return (T)system;
		if (IRegionSystem.class == adapter)   return (T)system;
		if (IToolPageSystem.class == adapter) return system.getAdapter(adapter);
		return super.getAdapter(adapter);
	}

	@Override
	public void setFocus() {
		if (system!=null) system.setFocus();
	}

	@Override
	public void dispose() {
		getSite().getPage().removeSelectionListener(this);
		if (system!=null) {
		    system.removeRegionListener(regionListener);
		    for (IRegion region : system.getRegions()) {
		    	region.removeROIListener(roiListener);
			}
			system.dispose();
		}
 		super.dispose();
	}

}
