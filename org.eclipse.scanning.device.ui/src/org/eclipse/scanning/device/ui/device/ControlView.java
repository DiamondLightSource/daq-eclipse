package org.eclipse.scanning.device.ui.device;


import java.net.URI;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scanning.api.ISpringParser;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.scan.ui.ControlTree;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.DevicePreferenceConstants;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.eclipse.scanning.device.ui.device.scannable.ControlTreeViewer;
import org.eclipse.scanning.device.ui.util.Stashing;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlView extends ViewPart {
	
	private static final Logger logger = LoggerFactory.getLogger(ControlView.class);

	public static final String ID = "org.eclipse.scanning.device.ui.device.ControlView"; //$NON-NLS-1$
	
	// UI
	private ControlTreeViewer viewer;

	// File
	private Stashing stash;

	public ControlView() {
		Activator.getDefault().getPreferenceStore().setDefault(DevicePreferenceConstants.SHOW_CONTROL_TOOLTIPS, true);
		this.stash = new Stashing("org.eclipse.scanning.device.ui.device.controls.json", ServiceHolder.getEventConnectorService());
	}
	
	@Override
    public void saveState(IMemento memento) {
    	super.saveState(memento);
    	try {
    		stash.stash(viewer.getControlTree());
		} catch (Exception e) {
			logger.error("Problem stashing control factory!", e);
		}
    }

	/** 
	 * We ensure that the xml is parsed, if any
	 * Hopefully this has already been done by
	 * the client spring xml configuration but
	 * if not we check if there is an xml argument
	 * here and attempt to load its path.
	 * This step is done for testing and to make
	 * the example client work. 
	 **/
	private ControlTree parseDefaultXML() {
		
		if (ControlTree.getInstance()!=null) return ControlTree.getInstance();
		String[] args = Platform.getApplicationArgs();
		for (int i = 0; i < args.length; i++) {
			final String arg = args[i];
			if (arg.equals("-xml")) {
				String path = args[i+1];
				ISpringParser parser = ServiceHolder.getSpringParser();
				try {
					parser.parse(path);
				} catch (Exception e) {
					logger.error("Unabled to parse: "+path, e);
				}
				break;
			}
		}
		return ControlTree.getInstance();
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		
		try {
			IScannableDeviceService cservice = ServiceHolder.getEventService().createRemoteService(new URI(Activator.getJmsUri()), IScannableDeviceService.class);

			ControlTree defaultTree = parseDefaultXML();
			if (defaultTree==null) {
				defaultTree = new ControlTree();
				defaultTree.globalize();
			}
			viewer = new ControlTreeViewer(defaultTree, cservice); // Widget linked to hardware, use ControlViewerMode.INDIRECT_NO_SET_VALUE to edit without setting hardware.
			
			ControlTree stashedTree = stash.unstash(ControlTree.class); // Or null if couldn't
			stashedTree.build();
			viewer.createPartControl(parent, stashedTree, getViewSite().getActionBars().getMenuManager(), getViewSite().getActionBars().getToolBarManager());
		
		    getSite().setSelectionProvider(viewer.getSelectionProvider());

		} catch (Exception e) {
			logger.error("Cannot build ControlTreeViewer!", e);
		}

	}

	@Override
	public void setFocus() {
		viewer.setFocus();
	}
	
	@Override
	public void dispose() {
		super.dispose();
		viewer.dispose();
	}
	
}
