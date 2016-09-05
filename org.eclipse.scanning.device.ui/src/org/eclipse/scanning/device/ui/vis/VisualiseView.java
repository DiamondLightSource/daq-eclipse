package org.eclipse.scanning.device.ui.vis;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.eclipse.swt.widgets.Composite;
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
public class VisualiseView extends ViewPart implements IAdaptable {
	
	public static final String ID = "org.eclipse.scanning.device.ui.vis.visualiseView";
	
	private static final Logger logger = LoggerFactory.getLogger(VisualiseView.class);
	
	protected IPlottingSystem<Composite>     system;

	public VisualiseView() {
		try {
			IPlottingService service = ServiceHolder.getPlottingService();
			system = service.createPlottingSystem();

		} catch (Exception ne) {
			logger.error("Unable to make plotting system", ne);
			system = null; // It creates the view but there will be no plotting system 
		}

	}

	@Override
	public void createPartControl(Composite parent) {

		system.createPlotPart(parent, getPartName(), getViewSite().getActionBars(), PlotType.IMAGE, this);  

	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (IPlottingSystem.class == adapter) return system;
		if (IToolPageSystem.class == adapter) return system.getAdapter(adapter);
		return super.getAdapter(adapter);
	}

	@Override
	public void setFocus() {
		if (system!=null) system.setFocus();
	}

	@Override
	public void dispose() {
		if (system!=null) system.dispose();
		super.dispose();
	}
	
}
