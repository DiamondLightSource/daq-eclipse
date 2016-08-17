package org.eclipse.scanning.device.ui.device;


import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.richbeans.widgets.internal.GridUtils;
import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.ISpringParser;
import org.eclipse.scanning.api.scan.ui.ControlFactory;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlView extends ViewPart {
	
	private static final Logger logger = LoggerFactory.getLogger(ControlView.class);

	public static final String ID = "org.eclipse.scanning.device.ui.device.ControlView"; //$NON-NLS-1$
	
	private FilteredTree viewer;

	public ControlView() {
		
		if (ControlFactory.getInstance().isEmpty()) {
			// We ensure that the xml is parsed, if any
			// Hopefully this has already been done by
			// the client spring xml configuration but
			// if not we check if there is an xml argument
			// here and attempt to load its path.
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
		}
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		
		viewer = new FilteredTree(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE, new NamedNodeFilter(), true);
		
		TreeViewer tviewer = viewer.getViewer();
		tviewer.getTree().setLinesVisible(true);
		tviewer.getTree().setHeaderVisible(false);
		viewer.setLayoutData(new GridData(GridData.FILL_BOTH));

		createColumns(tviewer);
		
		try {
			tviewer.setContentProvider(new ControlContentProvider());
		} catch (Exception e) {
			logger.error("Cannot create content provider", e);
		}
		tviewer.setInput(ControlFactory.getInstance());
		tviewer.expandAll();
		
		getSite().setSelectionProvider(tviewer);
		createActions();
		setSearchVisible(false);
	}

	private void setSearchVisible(boolean b) {
		GridUtils.setVisible(viewer.getFilterControl().getParent(), b);
		viewer.layout(new Control[]{viewer.getFilterControl().getParent()});
	}

	private void createColumns(TreeViewer viewer) {
		
		viewer.setColumnProperties(new String[] { "Name", "Value"});
		ColumnViewerToolTipSupport.enableFor(viewer);
		
        TreeViewerColumn var   = new TreeViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setText("Name");
		var.getColumn().setWidth(200);
		var.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				return ((INameable)element).getName();
			}
		});
		
		var   = new TreeViewerColumn(viewer, SWT.LEFT, 1);
		var.getColumn().setText("Value");
		var.getColumn().setWidth(300);
		var.setLabelProvider(new DelegatingStyledCellLabelProvider(new ControlValueLabelProvider()));
		
//		ColumnLabelProvider prov = new ModelFieldLabelProvider(this);
//		var.setLabelProvider(prov);
//		var.setEditingSupport(new ModelFieldEditingSupport(this, viewer, prov));

	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		
		IMenuManager    menuManager    = getViewSite().getActionBars().getMenuManager();
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
		
		IAction expandAll = new Action("Expand All", Activator.getImageDescriptor("icons/expand_all.png")) {
			public void run() {
				viewer.getViewer().expandAll();
			}
		};
		toolbarManager.add(expandAll);
		menuManager.add(expandAll);
		
		IAction showSearch = new Action("Expand All", IAction.AS_CHECK_BOX) {
			public void run() {
				setSearchVisible(isChecked());
			}
		};
		showSearch.setImageDescriptor(Activator.getImageDescriptor("icons/magnifier--pencil.png"));
		toolbarManager.add(showSearch);
		menuManager.add(showSearch);
	
	}
	
	@Override
	public void setFocus() {
		// Set the focus
	}

}
