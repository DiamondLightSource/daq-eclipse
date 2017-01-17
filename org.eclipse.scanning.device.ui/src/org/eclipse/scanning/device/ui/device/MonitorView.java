package org.eclipse.scanning.device.ui.device;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.util.ViewUtil;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * A view for adding and removing monitors. The monitors produced are
 * provided by the adaptable pattern as a list of strings. This list of
 * strings will be added to the ScanRequest as monitor names when the
 * scan is generated.
 * 
 * @author Matthew Gerring
 *
 */
public class MonitorView extends ViewPart {

	public static final String ID = "org.eclipse.scanning.device.ui.device.MonitorView"; //$NON-NLS-1$
	private static final Logger logger = LoggerFactory.getLogger(MonitorView.class);
	
	private ScannableViewer viewer;

	public MonitorView() {
		this.viewer = new ScannableViewer();
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		
		viewer.createPartControl(parent);
		
		getSite().setSelectionProvider(viewer.getSelectionProvider());
		
		createActions(getViewSite().getActionBars().getToolBarManager(), getViewSite().getActionBars().getMenuManager());
	}
    
	/**
	 * Create the actions.
	 */
	private void createActions(IContributionManager... managers) {
		
		List<IContributionManager> mans = new ArrayList<>(Arrays.asList(managers));
		MenuManager     rightClick     = new MenuManager();
		mans.add(rightClick);
		// Action to add a new ControlNode
		final IAction addNode = new Action("Add monitor", Activator.getImageDescriptor("icons/ui-toolbar--plus.png")) {
			public void run() {
				viewer.addScannable();
			}
		};
		
		// Action to remove the currently selected ControlNode or ControlGroup
		final IAction removeNode = new Action("Remove monitor", Activator.getImageDescriptor("icons/ui-toolbar--minus.png")) {
			public void run() {
				try {
					viewer.removeScannable();
				} catch (ScanningException e) {
					logger.error("Problem removing monitor from "+getClass().getSimpleName(), e);
				}
			}
		};
		removeNode.setEnabled(false);
			
		ViewUtil.addGroups("add", mans, addNode, removeNode);
		
		viewer.getControl().setMenu(rightClick.createContextMenu(viewer.getControl()));
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IScannable<?> scannable = viewer.getSelection();
				removeNode.setEnabled(scannable != null); // can only remove node if one is selected
			}
		});
		
		IAction refresh = new Action("Refresh", Activator.getImageDescriptor("icons/recycle.png")) {
			public void run() {
				try {
					viewer.refresh();
				} catch (Exception e) {
					logger.error("Problem refreshing from server!", e);
				}
			}
		};
		
		ViewUtil.addGroups("refresh", mans, refresh);
	}

	@Override
	public void setFocus() {
		viewer.setFocus();
	}

	@Override
	public void dispose() {
		viewer.dispose();
	}
}
