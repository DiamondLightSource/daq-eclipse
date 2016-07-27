package org.eclipse.scanning.device.ui.device;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.EventConnectionView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shows a list of available detectors to the user. 
 * They may click on one and configure it.
 * 
 * @author Matthew Gerring
 *
 */
public class DetectorView extends EventConnectionView {

	private static final Logger logger = LoggerFactory.getLogger(DetectorView.class);
	public  static final String ID     = "org.eclipse.scanning.device.ui.detectorView";
	
	private TableViewer viewer;
	private Image defaultIcon;
	private Map<String,Image> iconMap;
	
	public DetectorView() {
		try {
		    this.defaultIcon = Activator.getImageDescriptor("icons/camera-lens.png").createImage();
		} catch (Throwable ne) {
			this.defaultIcon = null;
		}
		this.iconMap     = new HashMap<>(7);
	}
	
	@Override
	public void createPartControl(Composite parent) {
		
		viewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
		
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(false);
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));

		createColumns(viewer, "", "Name");
		
		try {
			viewer.setContentProvider(new DetectorContentProvider(getUri()));
		} catch (Exception e) {
			logger.error("Cannot create content provider", e);
		}
		viewer.setInput(new Object());
		
		getSite().setSelectionProvider(viewer);
		
		createActions();
		initializeToolBar();
		initializeMenu();

	}

	private void createColumns(TableViewer tableViewer, String icon, String name) {
		
		TableViewerColumn iconColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		iconColumn.getColumn().setWidth(20);
		iconColumn.getColumn().setMoveable(false);
		iconColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				if (!(element instanceof DeviceInformation)) return null;
				DeviceInformation info = (DeviceInformation)element;
				if (info.getIcon()==null) return defaultIcon;
				try {
					return getIcon(info.getIcon());
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
			}
		});

		TableViewerColumn nameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		nameColumn.getColumn().setWidth(300);
		nameColumn.getColumn().setMoveable(false);
		nameColumn.getColumn().setText(name);
		nameColumn.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				if (!(element instanceof DeviceInformation)) return null;
				DeviceInformation info = (DeviceInformation)element;
				String label = info.getLabel();
				if (label==null) label = info.getName();
				if (label==null) label = info.getId();
				if (label==null) label = "Unamed Device "+info;
 				return label;
			}
		});
		
	}

	protected Image getIcon(String fullPath) throws IOException {
		
		if (fullPath==null)      return defaultIcon;
		if ("".equals(fullPath)) return defaultIcon;
		
		try {
			if (iconMap.containsKey(fullPath)) return iconMap.get(fullPath);
			final String[] sa = fullPath.split("/");
			final Bundle bundle = Platform.getBundle(sa[0]);
			if (bundle!=null) {
				Image image = new Image(null, bundle.getResource(sa[1]+"/"+sa[2]).openStream());
				iconMap.put(fullPath, image);
			}
			return iconMap.get(fullPath);
		} catch (Exception ne) {
			logger.debug("Cannot get icon for "+fullPath, ne);
			return defaultIcon;
		}
	}

	@Override
	public void setFocus() {
		viewer.getTable().setFocus();
	}

	public static String createId(final String uri, final String requestName, final String responseName) {
		
		final StringBuilder buf = new StringBuilder();
		buf.append(ID);
		buf.append(":");
		buf.append(createSecondaryId(uri, requestName, responseName));
		return buf.toString();
	}
	/**
	 * Create the actions.
	 */
	private void createActions() {
		getViewSite().getActionBars().getToolBarManager().add(new Action("Refresh") {
			public void run() {
				viewer.refresh();
			}
		});
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		@SuppressWarnings("unused")
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		@SuppressWarnings("unused")
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
	}

	
	public void dispose() {
		super.dispose();
		defaultIcon.dispose();
		for (String path : iconMap.keySet()) iconMap.get(path).dispose();
		iconMap.clear();
	}
}
