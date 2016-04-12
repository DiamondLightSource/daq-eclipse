package org.eclipse.scanning.example.xcen.ui.views;

import java.io.File;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.plotting.api.VanillaPlottingSystemView;
import org.eclipse.scanning.example.xcen.ui.XcenActivator;
import org.eclipse.scanning.example.xcen.ui.XcenServices;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XcenDiagram extends VanillaPlottingSystemView {
	
	public static final String ID = "org.eclipse.scanning.example.xcen.ui.views.XcenDiagram";
	
	private static final Logger logger = LoggerFactory.getLogger(XcenDiagram.class);
	
	private ILoaderService service;
	
	public XcenDiagram() {
		super();
		service = XcenServices.getCurrent().getLoaderService();
	}

	@Override
	public void createPartControl(Composite parent) {
        super.createPartControl(parent);
     
        // TODO Hard coded an x-stall, should come from current data acquisition.
        try {
			final File loc = new File(BundleUtils.getBundleLocation(XcenActivator.PLUGIN_ID), "icons/xstall.png");
			final IDataset image = service.getDataset(loc.getAbsolutePath(), new IMonitor.Stub());

			system.createPlot2D(image, null, new NullProgressMonitor());
			
        } catch (Exception ne) {
        	logger.error("Cannot load dataset!", ne);
        }
	}
}
