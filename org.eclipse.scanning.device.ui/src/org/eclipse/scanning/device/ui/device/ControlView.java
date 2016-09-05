package org.eclipse.scanning.device.ui.device;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scanning.api.ISpringParser;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.scan.ui.ControlTree;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.DevicePreferenceConstants;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.eclipse.scanning.device.ui.device.scannable.ControlTreeViewer;
import org.eclipse.scanning.device.ui.device.scannable.ControlViewerMode;
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

	public ControlView() {
		Activator.getDefault().getPreferenceStore().setDefault(DevicePreferenceConstants.SHOW_CONTROL_TOOLTIPS, true);
	}
	
	@Override
    public void saveState(IMemento memento) {
    	super.saveState(memento);
    	try {
			stash(viewer.getControlTree(), ServiceHolder.getEventConnectorService());
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
			viewer = new ControlTreeViewer(defaultTree, cservice); // Widget linked to hardware, use ControlViewerMode.INDIRECT_NO_SET_VALUE to edit without setting hardware.
			
			ControlTree stashedTree = unstash(ServiceHolder.getEventConnectorService()); // Or null if couldn't
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
	
	
	private static File getStashFile() {
		final File stash = new File(System.getProperty("user.name")+"/.solstice/org.eclipse.scanning.device.ui.device.controls.json");
        return stash;
	}
	
	private static boolean isStashed() {
		return getStashFile().exists();
	}
	
	private void stash(ControlTree tree, IEventConnectorService marshallerService) throws Exception {
		final String json = marshallerService.marshal(tree);
		write(getStashFile(), json);
	}
	
	private static ControlTree unstash(IEventConnectorService marshallerService) {
		
		if (!isStashed()) return null;
		try {
			final String json = readFile(getStashFile()).toString();
			final ControlTree factory = marshallerService.unmarshal(json, ControlTree.class);
			factory.build();
			return factory;
		} catch (Exception ne) {
			logger.error("Cannot read file "+getStashFile(), ne);
			return null;
		}
	}
	
	private static void write(final File file, final String text) throws Exception {
		
		file.getParentFile().mkdirs();
		BufferedWriter b = null;
		try {
			final OutputStream out = new FileOutputStream(file);
			final OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
			b = new BufferedWriter(writer);
			b.write(text.toCharArray());
		} finally {
			if (b != null) {
				b.close();
			}
		}
	}

	private static final StringBuffer readFile(final File file) throws Exception {

		final String charsetName = "UTF-8";
		final InputStream in = new FileInputStream(file);
		BufferedReader ir = null;
		try {
			ir = new BufferedReader(new InputStreamReader(in, charsetName));

			// deliberately do not remove BOM here
			int c;
			StringBuffer currentStrBuffer = new StringBuffer();
			final char[] buf = new char[4096];
			while ((c = ir.read(buf, 0, 4096)) > 0) {
				currentStrBuffer.append(buf, 0, c);
			}
			return currentStrBuffer;

		} finally {
			if (ir != null) {
				ir.close();
			}
		}
	}

}
